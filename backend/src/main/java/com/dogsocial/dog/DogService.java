package com.dogsocial.dog;

import com.dogsocial.dog.dto.DogDtos;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DogService {
  private final DogRepository dogRepository;
  private final UserRepository userRepository;

  public DogService(DogRepository dogRepository, UserRepository userRepository) {
    this.dogRepository = dogRepository;
    this.userRepository = userRepository;
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
    dogRepository.delete(dog);
  }

  public Page<DogDtos.DogResponse> listByOwner(Long ownerId, Pageable pageable) {
    return dogRepository.findByOwnerId(ownerId, pageable).map(this::toDto);
  }

  private DogDtos.DogResponse toDto(Dog dog) {
    User owner = dog.getOwner();
    UserDtos.UserSummary ownerSummary = UserDtos.UserSummary.builder()
        .id(owner.getId())
        .email(owner.getEmail())
        .build();
    return DogDtos.DogResponse.builder()
        .id(dog.getId())
        .owner(ownerSummary)
        .name(dog.getName())
        .breed(dog.getBreed())
        .bio(dog.getBio())
        .createdAt(dog.getCreatedAt() == null ? null : dog.getCreatedAt().toString())
        .build();
  }
}

