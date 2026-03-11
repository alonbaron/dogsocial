package com.dogsocial.dog;

import com.dogsocial.dog.dto.DogDtos;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

  @PostMapping(value = "/{dogId}/photo", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public DogDtos.DogResponse uploadPhoto(
      @PathVariable Long dogId,
      @RequestParam("file") MultipartFile file
  ) {
    return dogService.uploadPhoto(dogId, file);
  }

  @GetMapping("/{dogId}/photo")
  public ResponseEntity<byte[]> getPhoto(@PathVariable Long dogId) {
    DogService.DogPhotoResult result = dogService.getPhoto(dogId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(result.contentType()))
        .body(result.data());
  }
}
