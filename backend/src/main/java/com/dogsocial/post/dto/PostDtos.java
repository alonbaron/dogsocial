package com.dogsocial.post.dto;

import com.dogsocial.dog.dto.DogDtos;
import com.dogsocial.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class PostDtos {
  @Data
  public static class CreatePostRequest {
    private Long dogId;

    @NotBlank
    @Size(max = 300)
    private String caption;
  }

  @Data
  public static class UpdatePostRequest {
    @NotBlank
    @Size(max = 300)
    private String caption;
  }

  @Data
  @Builder
  public static class PostResponse {
    private Long id;
    private UserDtos.UserSummary author;
    private DogSummary dog;
    private String caption;
    private String createdAt;
    private String updatedAt;
    private long likesCount;
    private long dislikesCount;
    private String myReaction; // LIKE/DISLIKE/NONE
  }

  @Data
  @Builder
  public static class DogSummary {
    private Long id;
    private String name;
  }
}

