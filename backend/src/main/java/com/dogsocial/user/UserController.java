package com.dogsocial.user;

import com.dogsocial.follow.FollowRepository;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public UserController(UserRepository userRepository, FollowRepository followRepository) {
    this.userRepository = userRepository;
    this.followRepository = followRepository;
  }

  @GetMapping
  public com.dogsocial.api.PageResponse<UserDtos.BrowseUser> browse(
      @RequestParam(value = "q", required = false) String q,
      @PageableDefault(size = 12) Pageable pageable
  ) {
    Long me = SecurityUtils.requireUserId();
    Set<Long> followed = new HashSet<>(followRepository.findFollowedIds(me));

    Page<User> page = (q == null || q.isBlank())
        ? userRepository.findAll(pageable)
        : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);

    Page<UserDtos.BrowseUser> dto = page.map(u -> UserDtos.BrowseUser.builder()
        .id(u.getId())
        .email(u.getEmail())
        .isMe(u.getId().equals(me))
        .isFollowing(followed.contains(u.getId()))
        .build());

    return com.dogsocial.api.PageResponse.of(dto);
  }

  @GetMapping("/{userId}")
  public UserDtos.UserProfile getProfile(@PathVariable Long userId) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    long followers = followRepository.countByFollowedId(userId);
    long following = followRepository.countByFollowerId(userId);

    boolean isMe = me.equals(userId);
    boolean isFollowing = !isMe && followRepository.existsByFollowerIdAndFollowedId(me, userId);

    return UserDtos.UserProfile.builder()
        .id(user.getId())
        .email(user.getEmail())
        .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().toString())
        .followersCount(followers)
        .followingCount(following)
        .isMe(isMe)
        .isFollowing(isFollowing)
        .build();
  }
}

