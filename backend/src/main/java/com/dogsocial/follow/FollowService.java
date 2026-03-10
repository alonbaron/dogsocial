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
    return followRepository.findByFollowerId(userId, pageable)
        .map(f -> UserDtos.UserSummary.builder()
            .id(f.getFollowed().getId())
            .email(f.getFollowed().getEmail())
            .build());
  }

  @Transactional(readOnly = true)
  public Page<UserDtos.UserSummary> getFollowers(Long userId, Pageable pageable) {
    return followRepository.findByFollowedId(userId, pageable)
        .map(f -> UserDtos.UserSummary.builder()
            .id(f.getFollower().getId())
            .email(f.getFollower().getEmail())
            .build());
  }
}
