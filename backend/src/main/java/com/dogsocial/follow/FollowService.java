package com.dogsocial.follow;

import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class FollowService {
  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  public FollowService(FollowRepository followRepository, UserRepository userRepository) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = false)
  public void follow(Long userId) {
    Long me = SecurityUtils.requireUserId();
    if (me.equals(userId)) {
      throw new BadRequestException("Cannot follow yourself");
    }
    User follower = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));
    User followed = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    if (followRepository.existsByFollowerIdAndFollowedId(me, userId)) {
      return; // already following
    }
    followRepository.save(Follow.builder().follower(follower).followed(followed).build());
  }

  @Transactional(readOnly = false)
  public void unfollow(Long userId) {
    Long me = SecurityUtils.requireUserId();
    followRepository.deleteByFollowerIdAndFollowedId(me, userId);
  }

  @Transactional(readOnly = true)
  public Page<UserDtos.UserSummary> getFollowing(Long userId, Pageable pageable) {
    // Every user in the "following" list is, by definition, followed by the viewer
    return followRepository.findByFollowerId(userId, pageable)
        .map(f -> UserDtos.UserSummary.builder()
            .id(f.getFollowed().getId())
            .username(f.getFollowed().getUsername())
            .isFollowing(true)
            .build());
  }

  @Transactional(readOnly = true)
  public Page<UserDtos.UserSummary> getFollowers(Long userId, Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    Page<Follow> page = followRepository.findByFollowedId(userId, pageable);
    List<Long> followerIds = page.getContent().stream().map(f -> f.getFollower().getId()).toList();
    Set<Long> iFollow = followerIds.isEmpty()
        ? Set.of()
        : followRepository.findFollowedIdsAmong(me, followerIds);
    return page.map(f -> UserDtos.UserSummary.builder()
        .id(f.getFollower().getId())
        .username(f.getFollower().getUsername())
        .isFollowing(iFollow.contains(f.getFollower().getId()))
        .build());
  }
}
