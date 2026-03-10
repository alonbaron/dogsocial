package com.dogsocial.friend;

import com.dogsocial.follow.FollowRepository;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendsService {
  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  public FriendsService(FollowRepository followRepository, UserRepository userRepository) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public Page<UserDtos.UserSummary> getFriends(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();

    Set<Long> following = new HashSet<>(followRepository.findFollowedIds(me));
    Set<Long> followers = new HashSet<>(followRepository.findFollowerIds(me));
    following.retainAll(followers);

    if (following.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0L);
    }

    List<Long> idList = new ArrayList<>(following);
    idList.sort(Comparator.naturalOrder());
    List<User> all = userRepository.findAllById(idList);
    all.sort(Comparator.comparing(User::getId));

    int start = (int) pageable.getOffset();
    int total = all.size();
    int end = Math.min(start + pageable.getPageSize(), total);
    List<User> pageContent = start >= total ? List.of() : all.subList(start, end);

    List<UserDtos.UserSummary> dtos = pageContent.stream()
        .map(u -> UserDtos.UserSummary.builder()
            .id(u.getId())
            .username(u.getUsername())
            .isFollowing(true) // friends are mutual follows — viewer always follows them
            .build())
        .collect(Collectors.toList());

    return new PageImpl<>(dtos, pageable, total);
  }
}
