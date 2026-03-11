package com.dogsocial.dog;

import com.dogsocial.dog.dto.DogDtos;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.image.ImageStoreService;
import com.dogsocial.image.StoredImage;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class DogService {
  private final DogRepository dogRepository;
  private final UserRepository userRepository;
  private final ImageStoreService imageStore;

  public DogService(DogRepository dogRepository, UserRepository userRepository, ImageStoreService imageStore) {
    this.dogRepository = dogRepository;
    this.userRepository = userRepository;
    this.imageStore = imageStore;
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
      try { imageStore.delete(Long.parseLong(dog.getPhotoPath())); } catch (Exception ignored) {}
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

    imageStore.validate(file);
    Long imageId = imageStore.save(file);

    if (dog.getPhotoPath() != null) {
      try { imageStore.delete(Long.parseLong(dog.getPhotoPath())); } catch (Exception ignored) {}
    }

    dog.setPhotoPath(String.valueOf(imageId));
    dog = dogRepository.save(dog);
    return toDto(dog);
  }

  public DogPhotoResult getPhoto(Long dogId) {
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    if (dog.getPhotoPath() == null || dog.getPhotoPath().isBlank()) {
      throw new NotFoundException("No photo for this dog");
    }
    StoredImage img = imageStore.get(Long.parseLong(dog.getPhotoPath()));
    return new DogPhotoResult(img.getData(), img.getContentType());
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

  public record DogPhotoResult(byte[] data, String contentType) {}
}
