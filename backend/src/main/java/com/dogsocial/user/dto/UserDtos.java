package com.dogsocial.user.dto;

import lombok.Builder;
import lombok.Data;

public class UserDtos {
  @Data
  @Builder
  public static class UserSummary {
    private Long id;
    private String email;
  }

  @Data
  @Builder
  public static class BrowseUser {
    private Long id;
    private String email;
    private boolean isMe;
    private boolean isFollowing;
  }

  @Data
  @Builder
  public static class UserProfile {
    private Long id;
    private String email;
    private String createdAt;
    private long followersCount;
    private long followingCount;
    private boolean isMe;
    private boolean isFollowing;
  }
}

