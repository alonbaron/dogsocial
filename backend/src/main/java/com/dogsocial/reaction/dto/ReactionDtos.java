package com.dogsocial.reaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ReactionDtos {
  public enum ReactionSelection {
    LIKE,
    DISLIKE,
    NONE
  }

  @Data
  public static class ReactionRequest {
    @NotNull
    private ReactionSelection type;
  }
}

