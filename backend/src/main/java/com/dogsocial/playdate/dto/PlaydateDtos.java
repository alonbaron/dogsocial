package com.dogsocial.playdate.dto;

import com.dogsocial.playdate.PlaydateStatus;
import com.dogsocial.user.dto.UserDtos;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public class PlaydateDtos {
  @Data
  public static class CreatePlaydateRequest {
    @NotNull
    private Long recipientId;

    @NotNull
    @Future
    private Instant scheduledAt;

    @Size(max = 300)
    private String note;
  }

  @Data
  public static class UpdateStatusRequest {
    @NotNull
    private PlaydateStatus status;
  }

  @Data
  @Builder
  public static class PlaydateResponse {
    private Long id;
    private UserDtos.UserSummary requester;
    private UserDtos.UserSummary recipient;
    private Instant scheduledAt;
    private PlaydateStatus status;
    private String note;
    private String createdAt;
    private String updatedAt;
    private boolean isRequester;
  }
}

