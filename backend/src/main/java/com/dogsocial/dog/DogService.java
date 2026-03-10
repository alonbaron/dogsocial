package com.dogsocial.dog;

import com.dogsocial.config.AppProperties;
import com.dogsocial.dog.dto.DogDtos;
import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DogService {
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
  private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

  private final DogRepository dogRepository;
  private final UserRepository userRepository;
  private final Path uploadRoot;

  public DogService(DogRepository dogRepository, UserRepository userRepository, AppProperties appProperties) {
    this.dogRepository = dogRepository;
    this.userRepository = userRepository;
    this.uploadRoot = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
  }

  @Transactional(readOnly = false)
  public DogDtos.DogResponse create(DogDtos.CreateDogRequest req) {
    Long me = SecurityUtils.requireUserId();
    User owner = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    Dog dog = Dog.builder()
        .owner(owner)
        .name(req.getName())
        .breed(req.getBreed())
        .bio(req.getBio())
        .build();
    dog = dogRepository.save(dog);
    return toDto(dog);
  }

  public DogDtos.DogResponse get(Long dogId) {
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    return toDto(dog);
  }

  @Transactional(readOnly = false)
  public DogDtos.DogResponse update(Long dogId, DogDtos.UpdateDogRequest req) {
    Long me = SecurityUtils.requireUserId();
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    if (!dog.getOwner().getId().equals(me)) {
      throw new ForbiddenException("Only the owner can update this dog");
    }
    dog.setName(req.getName());
    dog.setBreed(req.getBreed());
    dog.setBio(req.getBio());
    dog = dogRepository.save(dog);
    return toDto(dog);
  }

  @Transactional(readOnly = false)
  public void delete(Long dogId) {
    Long me = SecurityUtils.requireUserId();
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    if (!dog.getOwner().getId().equals(me)) {
      throw new ForbiddenException("Only the owner can delete this dog");
    }
    if (dog.getPhotoPath() != null) {
      deleteFile(dog.getPhotoPath());
    }
    dogRepository.delete(dog);
  }

  public Page<DogDtos.DogResponse> listByOwner(Long ownerId, Pageable pageable) {
    return dogRepository.findByOwnerId(ownerId, pageable).map(this::toDto);
  }

  @Transactional(readOnly = false)
  public DogDtos.DogResponse uploadPhoto(Long dogId, MultipartFile file) {
    Long me = SecurityUtils.requireUserId();
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    if (!dog.getOwner().getId().equals(me)) {
      throw new ForbiddenException("Only the owner can upload a photo");
    }
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

    String ext = contentType.split("/")[1];
    if ("jpeg".equals(ext)) ext = "jpg";
    String filename = "dogs/" + dogId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    Path target = uploadRoot.resolve(filename);

    try {
      Files.createDirectories(target.getParent());
      file.transferTo(target.toFile());
    } catch (IOException e) {
      throw new BadRequestException("Failed to save file: " + e.getMessage());
    }

    if (dog.getPhotoPath() != null) {
      deleteFile(dog.getPhotoPath());
    }
    dog.setPhotoPath(filename);
    dog = dogRepository.save(dog);
    return toDto(dog);
  }

  public DogPhotoResult getPhoto(Long dogId) {
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    if (dog.getPhotoPath() == null || dog.getPhotoPath().isBlank()) {
      throw new NotFoundException("No photo for this dog");
    }
    Path path = uploadRoot.resolve(dog.getPhotoPath()).normalize();
    if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) {
      throw new NotFoundException("Dog photo file not found");
    }
    String contentType = "image/jpeg";
    String fp = dog.getPhotoPath();
    if (fp.endsWith(".png")) contentType = "image/png";
    else if (fp.endsWith(".gif")) contentType = "image/gif";
    else if (fp.endsWith(".webp")) contentType = "image/webp";
    return new DogPhotoResult(new PathResource(path), contentType);
  }

  private void deleteFile(String relativePath) {
    try {
      Path p = uploadRoot.resolve(relativePath).normalize();
      if (p.startsWith(uploadRoot)) Files.deleteIfExists(p);
    } catch (IOException ignored) {
    }
  }

  private DogDtos.DogResponse toDto(Dog dog) {
    User owner = dog.getOwner();
    UserDtos.UserSummary ownerSummary = UserDtos.UserSummary.builder()
        .id(owner.getId())
        .username(owner.getUsername())
        .build();
    String photoUrl = dog.getPhotoPath() != null ? "/dogs/" + dog.getId() + "/photo" : null;
    return DogDtos.DogResponse.builder()
        .id(dog.getId())
        .owner(ownerSummary)
        .name(dog.getName())
        .breed(dog.getBreed())
        .bio(dog.getBio())
        .photoUrl(photoUrl)
        .createdAt(dog.getCreatedAt() == null ? null : dog.getCreatedAt().toString())
        .build();
  }

  public record DogPhotoResult(Resource resource, String contentType) {}
}
