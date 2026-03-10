package com.dogsocial.dog.dto;

import com.dogsocial.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class DogDtos {
  @Data
  public static class CreateDogRequest {
    @NotBlank
    @Size(max = 80)
    private String name;

    @Size(max = 80)
    private String breed;

    @Size(max = 300)
    private String bio;
  }

  @Data
  public static class UpdateDogRequest {
    @NotBlank
    @Size(max = 80)
    private String name;

    @Size(max = 80)
    private String breed;

    @Size(max = 300)
    private String bio;
  }

  @Data
  @Builder
  public static class DogResponse {
    private Long id;
    private UserDtos.UserSummary owner;
    private String name;
    private String breed;
    private String bio;
    private String photoUrl;
    private String createdAt;
  }
}

