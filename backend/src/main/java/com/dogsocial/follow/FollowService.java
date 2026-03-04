package com.dogsocial.follow;

import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {
  private final FollowRepository followRepository;

  public FollowService(FollowRepository followRepository) {
    this.followRepository = followRepository;
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
