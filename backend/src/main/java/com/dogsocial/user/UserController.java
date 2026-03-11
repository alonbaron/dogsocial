package com.dogsocial.user;

import com.dogsocial.exception.BadRequestException;
import com.dogsocial.follow.FollowRepository;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.dto.UserDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;
  private final FollowRepository followRepository;
  private final AvatarService avatarService;

  public UserController(UserRepository userRepository, FollowRepository followRepository, AvatarService avatarService) {
    this.userRepository = userRepository;
    this.followRepository = followRepository;
    this.avatarService = avatarService;
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
        : userRepository.searchByEmailOrUsername(q.trim(), pageable);

    Page<UserDtos.BrowseUser> dto = page.map(u -> UserDtos.BrowseUser.builder()
        .id(u.getId())
        .username(u.getUsername())
        .isMe(u.getId().equals(me))
        .isFollowing(followed.contains(u.getId()))
        .build());

    return com.dogsocial.api.PageResponse.of(dto);
  }

  @GetMapping("/by-username/{username}")
  public UserDtos.UserProfile getProfileByUsername(@PathVariable String username) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found"));
    return buildProfile(user, me);
  }

  @GetMapping("/{userId}")
  public UserDtos.UserProfile getProfile(@PathVariable Long userId) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
    return buildProfile(user, me);
  }

  @PutMapping("/me/profile")
  public UserDtos.UserProfile updateProfile(@Valid @RequestBody UserDtos.UpdateProfileRequest req) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findById(me)
        .orElseThrow(() -> new NotFoundException("User not found"));

    if (req.getUsername() != null && !req.getUsername().isBlank()) {
      String newUsername = req.getUsername().trim();
      if (!newUsername.equals(user.getUsername())) {
        if (userRepository.existsByUsername(newUsername)) {
          throw new BadRequestException("Username already taken");
        }
        user.setUsername(newUsername);
      }
    }

    if (req.getBio() != null) {
      user.setBio(req.getBio().isBlank() ? null : req.getBio().trim());
    }

    user = userRepository.save(user);
    return buildProfile(user, me);
  }

  @PostMapping("/me/avatar")
  public void uploadAvatar(@RequestParam("file") MultipartFile file) {
    avatarService.uploadAvatar(file);
  }

  @GetMapping("/{userId}/avatar")
  public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
    AvatarService.ImageResult result = avatarService.getAvatar(userId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(result.contentType()))
        .body(result.data());
  }

  private UserDtos.UserProfile buildProfile(User user, Long me) {
    long followers = followRepository.countByFollowedId(user.getId());
    long following = followRepository.countByFollowerId(user.getId());
    boolean isMe = me.equals(user.getId());
    boolean isFollowing = !isMe && followRepository.existsByFollowerIdAndFollowedId(me, user.getId());

    return UserDtos.UserProfile.builder()
        .id(user.getId())
        .email(isMe ? user.getEmail() : null)   // only expose email to the owner
        .username(user.getUsername())
        .bio(user.getBio())
        .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().toString())
        .followersCount(followers)
        .followingCount(following)
        .isMe(isMe)
        .isFollowing(isFollowing)
        .build();
  }
}
