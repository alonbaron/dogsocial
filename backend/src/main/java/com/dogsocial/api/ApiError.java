package com.dogsocial.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiError {
  private String timestamp;
  private int status;
  private String error;
  private String message;
  /** Optional detail (e.g. exception message) for 500 errors to aid debugging. */
  private String detail;
  private String path;
  private List<FieldError> fieldErrors;

  @Data
  @Builder
  public static class FieldError {
    private String field;
    private String message;
  }
}

