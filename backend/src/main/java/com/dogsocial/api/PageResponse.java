package com.dogsocial.api;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {
  private List<T> items;
  private int page;
  private int size;
  private long totalItems;
  private int totalPages;
  private boolean hasNext;

  public static <T> PageResponse<T> of(Page<T> page) {
    return PageResponse.<T>builder()
        .items(page.getContent())
        .page(page.getNumber())
        .size(page.getSize())
        .totalItems(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .hasNext(page.hasNext())
        .build();
  }
}

