package com.dogsocial.user;

import com.dogsocial.exception.NotFoundException;
import com.dogsocial.image.ImageStoreService;
import com.dogsocial.image.StoredImage;
import com.dogsocial.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarService {
  private final UserRepository userRepository;
  private final ImageStoreService imageStore;

  public AvatarService(UserRepository userRepository, ImageStoreService imageStore) {
    this.userRepository = userRepository;
    this.imageStore = imageStore;
  }

  @Transactional
  public void uploadAvatar(MultipartFile file) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    imageStore.validate(file);
    Long imageId = imageStore.save(file);

    if (user.getAvatarPath() != null) {
      try { imageStore.delete(Long.parseLong(user.getAvatarPath())); } catch (Exception ignored) {}
    }

    user.setAvatarPath(String.valueOf(imageId));
    userRepository.save(user);
  }

  public ImageResult getAvatar(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
      throw new NotFoundException("No avatar");
    }
    StoredImage img = imageStore.get(Long.parseLong(user.getAvatarPath()));
    return new ImageResult(img.getData(), img.getContentType());
  }

  public record ImageResult(byte[] data, String contentType) {}
}
