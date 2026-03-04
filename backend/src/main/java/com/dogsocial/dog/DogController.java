package com.dogsocial.dog;

import com.dogsocial.dog.dto.DogDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dogs")
public class DogController {
  private final DogService dogService;

  public DogController(DogService dogService) {
    this.dogService = dogService;
  }

  @PostMapping
  public DogDtos.DogResponse create(@Valid @RequestBody DogDtos.CreateDogRequest req) {
    return dogService.create(req);
  }

  @GetMapping("/{dogId}")
  public DogDtos.DogResponse get(@PathVariable Long dogId) {
    return dogService.get(dogId);
  }

  @PutMapping("/{dogId}")
  public DogDtos.DogResponse update(@PathVariable Long dogId, @Valid @RequestBody DogDtos.UpdateDogRequest req) {
    return dogService.update(dogId, req);
  }

  @DeleteMapping("/{dogId}")
  public void delete(@PathVariable Long dogId) {
    dogService.delete(dogId);
  }
}

