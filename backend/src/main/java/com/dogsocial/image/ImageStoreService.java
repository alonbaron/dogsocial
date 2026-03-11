package com.dogsocial.image;

import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class ImageStoreService {
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
  private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

  private final StoredImageRepository repository;

  public ImageStoreService(StoredImageRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public Long save(MultipartFile file) {
    validate(file);
    try {
      StoredImage image = StoredImage.builder()
          .data(file.getBytes())
          .contentType(file.getContentType())
          .build();
      return repository.save(image).getId();
    } catch (IOException e) {
      throw new BadRequestException("Failed to read uploaded file");
    }
  }

  @Transactional(readOnly = true)
  public StoredImage get(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Image not found"));
  }

  @Transactional
  public void delete(Long id) {
    repository.deleteById(id);
  }

  public void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("No file provided");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new BadRequestException("Invalid file type. Use JPEG, PNG, GIF, or WebP.");
    }
    if (file.getSize() > MAX_SIZE_BYTES) {
      throw new BadRequestException("File too large. Maximum size is 5 MB.");
    }
  }
}
