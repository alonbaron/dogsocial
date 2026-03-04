package com.dogsocial.user;

import com.dogsocial.api.PageResponse;
import com.dogsocial.follow.FollowService;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserFollowsController {
  private final FollowService followService;

  public UserFollowsController(FollowService followService) {
    this.followService = followService;
  }

  @GetMapping("/{userId}/following")
  public PageResponse<UserDtos.UserSummary> following(
      @PathVariable Long userId,
      @PageableDefault(size = 12) Pageable pageable
  ) {
    Page<UserDtos.UserSummary> page = followService.getFollowing(userId, pageable);
    return PageResponse.of(page);
  }

  @GetMapping("/{userId}/followers")
  public PageResponse<UserDtos.UserSummary> followers(
      @PathVariable Long userId,
      @PageableDefault(size = 12) Pageable pageable
  ) {
    Page<UserDtos.UserSummary> page = followService.getFollowers(userId, pageable);
    return PageResponse.of(page);
  }
}

