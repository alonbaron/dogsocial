package com.dogsocial.follow;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
  private final FollowService followService;

  public FollowController(FollowService followService) {
    this.followService = followService;
  }

  @PostMapping("/{userId}")
  public void follow(@PathVariable Long userId) {
    followService.follow(userId);
  }

  @DeleteMapping("/{userId}")
  public void unfollow(@PathVariable Long userId) {
    followService.unfollow(userId);
  }
}

