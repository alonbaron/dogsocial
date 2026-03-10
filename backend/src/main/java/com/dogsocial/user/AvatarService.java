package com.dogsocial.user;

import com.dogsocial.config.AppProperties;
import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.security.SecurityUtils;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
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
public class AvatarService {
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
  private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

  private final UserRepository userRepository;
  private final Path uploadRoot;

  public AvatarService(UserRepository userRepository, AppProperties appProperties) {
    this.userRepository = userRepository;
    this.uploadRoot = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
  }

  @Transactional(readOnly = false)
  public void uploadAvatar(MultipartFile file) {
    Long me = SecurityUtils.requireUserId();
    User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

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
    String filename = "profile/" + me + "_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    Path target = uploadRoot.resolve(filename);

    try {
      Files.createDirectories(target.getParent());
      file.transferTo(target.toFile());
    } catch (IOException e) {
      throw new BadRequestException("Failed to save file: " + e.getMessage());
    }

    if (user.getAvatarPath() != null) {
      Path oldPath = uploadRoot.resolve(user.getAvatarPath());
      try {
        Files.deleteIfExists(oldPath);
      } catch (IOException ignored) {
      }
    }

    user.setAvatarPath(filename);
    userRepository.save(user);
  }

  public Resource getAvatar(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
      throw new NotFoundException("No avatar");
    }
    Path path = uploadRoot.resolve(user.getAvatarPath()).normalize();
    if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) {
      throw new NotFoundException("Avatar file not found");
    }
    return new PathResource(path);
  }
}
