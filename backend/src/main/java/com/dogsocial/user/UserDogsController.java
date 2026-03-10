package com.dogsocial.user;

import com.dogsocial.api.PageResponse;
import com.dogsocial.dog.DogService;
import com.dogsocial.dog.dto.DogDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserDogsController {
  private final DogService dogService;

  public UserDogsController(DogService dogService) {
    this.dogService = dogService;
  }

  @GetMapping("/{userId}/dogs")
  public PageResponse<DogDtos.DogResponse> listDogs(
      @PathVariable Long userId,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Page<DogDtos.DogResponse> page = dogService.listByOwner(userId, pageable);
    return PageResponse.of(page);
  }
}

