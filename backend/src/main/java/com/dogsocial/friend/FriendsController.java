package com.dogsocial.friend;

import com.dogsocial.api.PageResponse;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {
  private final FriendsService friendsService;

  public FriendsController(FriendsService friendsService) {
    this.friendsService = friendsService;
  }

  @GetMapping
  public PageResponse<UserDtos.UserSummary> friends(@PageableDefault(size = 12) Pageable pageable) {
    Page<UserDtos.UserSummary> page = friendsService.getFriends(pageable);
    return PageResponse.of(page);
  }
}

