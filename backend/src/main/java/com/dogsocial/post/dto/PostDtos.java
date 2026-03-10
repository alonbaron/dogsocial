package com.dogsocial.post.dto;

import com.dogsocial.user.dto.UserDtos;
import lombok.Builder;
import lombok.Data;

public class PostDtos {
  @Data
  public static class UpdatePostRequest {
    private String caption;
  }

  @Data
  @Builder
  public static class PostResponse {
    private Long id;
    private UserDtos.UserSummary author;
    private DogSummary dog;
    private String caption;
    private String imageUrl;
    private String createdAt;
    private String updatedAt;
    private long likesCount;
    private long dislikesCount;
    private String myReaction;
  }

  @Data
  @Builder
  public static class DogSummary {
    private Long id;
    private String name;
  }
}
