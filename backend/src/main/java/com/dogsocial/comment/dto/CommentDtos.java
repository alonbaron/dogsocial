package com.dogsocial.comment.dto;

import com.dogsocial.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class CommentDtos {
  @Data
  public static class CreateCommentRequest {
    @NotBlank
    @Size(max = 300)
    private String content;
  }

  @Data
  public static class UpdateCommentRequest {
    @NotBlank
    @Size(max = 300)
    private String content;
  }

  @Data
  @Builder
  public static class CommentResponse {
    private Long id;
    private Long postId;
    private UserDtos.UserSummary author;
    private String content;
    private String createdAt;
    private String updatedAt;
    private long likesCount;
    private long dislikesCount;
    private String myReaction; // LIKE/DISLIKE/NONE
  }
}

