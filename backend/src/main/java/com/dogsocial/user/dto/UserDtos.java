package com.dogsocial.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class UserDtos {
  @Data
  @Builder
  public static class UserSummary {
    private Long id;
    private String username;

    /**
     * Whether the currently authenticated viewer follows this user.
     * Uses boxed Boolean so Lombok generates getIsFollowing() → Jackson serializes as "isFollowing".
     * Null (not set) when follow context is irrelevant (posts, playdates, comments).
     */
    private Boolean isFollowing;
  }

  @Data
  @Builder
  public static class BrowseUser {
    private Long id;
    private String username;

    @JsonProperty("isMe")
    private boolean isMe;

    @JsonProperty("isFollowing")
    private boolean isFollowing;
  }

  @Data
  @Builder
  public static class UserProfile {
    private Long id;
    private String email;      // only sent to the owner (filtered in controller)
    private String username;
    private String bio;
    private String createdAt;
    private long followersCount;
    private long followingCount;

    @JsonProperty("isMe")
    private boolean isMe;

    @JsonProperty("isFollowing")
    private boolean isFollowing;
  }

  @Data
  public static class UpdateProfileRequest {
    @Pattern(
        regexp = "^[a-zA-Z0-9_]{3,30}$",
        message = "Username must be 3-30 characters: letters, numbers, underscores only"
    )
    private String username;

    @Size(max = 300)
    private String bio;
  }
}
