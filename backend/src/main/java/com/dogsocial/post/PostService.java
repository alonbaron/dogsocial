package com.dogsocial.post;

import com.dogsocial.config.AppProperties;
import com.dogsocial.dog.Dog;
import com.dogsocial.dog.DogRepository;
import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.follow.FollowRepository;
import com.dogsocial.post.dto.PostDtos;
import com.dogsocial.reaction.PostReaction;
import com.dogsocial.reaction.PostReactionRepository;
import com.dogsocial.reaction.ReactionType;
import com.dogsocial.reaction.dto.ReactionDtos;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {
  private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
  private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5MB

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final DogRepository dogRepository;
  private final FollowRepository followRepository;
  private final PostReactionRepository postReactionRepository;
  private final Path uploadRoot;

  public PostService(
      PostRepository postRepository,
      UserRepository userRepository,
      DogRepository dogRepository,
      FollowRepository followRepository,
      PostReactionRepository postReactionRepository,
      AppProperties appProperties
  ) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.dogRepository = dogRepository;
    this.followRepository = followRepository;
    this.postReactionRepository = postReactionRepository;
    this.uploadRoot = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
  }

  public Page<PostDtos.PostResponse> feed(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    List<Long> followed = followRepository.findFollowedIds(me);
    if (followed == null || followed.isEmpty()) {
      return hydrate(postRepository.findByAuthorId(me, pageable), me);
    }
    return hydrate(postRepository.feed(me, followed, pageable), me);
  }

  public Page<PostDtos.PostResponse> allPosts(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return hydrate(postRepository.findAllPostsOrderByCreatedAtDesc(pageable), me);
  }

  public Page<PostDtos.PostResponse> postsForUser(Long userId, Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return hydrate(postRepository.findByAuthorId(userId, pageable), me);
  }

  public Page<PostDtos.PostResponse> postsForDog(Long dogId, Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    Dog dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
    return hydrate(postRepository.findByDogId(dog.getId(), pageable), me);
  }

  public PostDtos.PostResponse get(Long postId) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    return hydrateSingle(post, me);
  }

  @Transactional(readOnly = false)
  public PostDtos.PostResponse create(String caption, Long dogId, MultipartFile image) {
    if (caption == null || caption.isBlank()) {
      throw new BadRequestException("Caption is required");
    }
    if (caption.length() > 300) {
      throw new BadRequestException("Caption must be 300 characters or fewer");
    }

    Long me = SecurityUtils.requireUserId();
    User author = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    Dog dog = null;
    if (dogId != null) {
      dog = dogRepository.findById(dogId).orElseThrow(() -> new NotFoundException("Dog not found"));
      if (!dog.getOwner().getId().equals(me)) {
        throw new ForbiddenException("You can only post as your own dog");
      }
    }

    Post post = Post.builder()
        .author(author)
        .dog(dog)
        .caption(caption.trim())
        .build();
    post = postRepository.save(post);

    if (image != null && !image.isEmpty()) {
      String imagePath = savePostImage(post.getId(), image);
      post.setImagePath(imagePath);
      post = postRepository.save(post);
    }

    return hydrateSingle(post, me);
  }

  @Transactional(readOnly = false)
  public PostDtos.PostResponse update(Long postId, PostDtos.UpdatePostRequest req) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    if (!post.getAuthor().getId().equals(me)) {
      throw new ForbiddenException("Only the author can update this post");
    }
    if (req.getCaption() != null && !req.getCaption().isBlank()) {
      post.setCaption(req.getCaption().trim());
    }
    post = postRepository.save(post);
    return hydrateSingle(post, me);
  }

  @Transactional(readOnly = false)
  public void delete(Long postId) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    if (!post.getAuthor().getId().equals(me)) {
      throw new ForbiddenException("Only the author can delete this post");
    }
    if (post.getImagePath() != null) {
      deletePostImage(post.getImagePath());
    }
    postRepository.delete(post);
  }

  @Transactional(readOnly = false)
  public PostDtos.PostResponse react(Long postId, ReactionDtos.ReactionRequest req) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

    ReactionDtos.ReactionSelection selection = req.getType();
    if (selection == ReactionDtos.ReactionSelection.NONE) {
      postReactionRepository.deleteByPostIdAndUserId(post.getId(), me);
      postReactionRepository.flush();
      return hydrateSingle(post, me);
    }

    ReactionType type = ReactionType.valueOf(selection.name());
    PostReaction reaction = postReactionRepository.findByPostIdAndUserId(post.getId(), me)
        .orElse(PostReaction.builder().post(post).user(userRepository.findById(me).orElseThrow()).build());
    reaction.setType(type);
    postReactionRepository.save(reaction);
    return hydrateSingle(post, me);
  }

  public PostImageResult getImage(Long postId) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    if (post.getImagePath() == null || post.getImagePath().isBlank()) {
      throw new NotFoundException("Post has no image");
    }
    Path path = uploadRoot.resolve(post.getImagePath()).normalize();
    if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) {
      throw new NotFoundException("Image file not found");
    }
    String contentType = "image/jpeg";
    String name = path.getFileName().toString();
    if (name.endsWith(".png")) contentType = "image/png";
    else if (name.endsWith(".gif")) contentType = "image/gif";
    else if (name.endsWith(".webp")) contentType = "image/webp";
    return new PostImageResult(new PathResource(path), contentType);
  }

  private String savePostImage(Long postId, MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
      throw new BadRequestException("Invalid image type. Use JPEG, PNG, GIF, or WebP.");
    }
    if (file.getSize() > MAX_IMAGE_BYTES) {
      throw new BadRequestException("Image too large. Maximum size is 5MB.");
    }
    String ext = contentType.split("/")[1];
    if ("jpeg".equals(ext)) ext = "jpg";
    String filename = "posts/" + postId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    Path target = uploadRoot.resolve(filename);
    try {
      Files.createDirectories(target.getParent());
      file.transferTo(target.toFile());
    } catch (IOException e) {
      throw new BadRequestException("Failed to save image: " + e.getMessage());
    }
    return filename;
  }

  private void deletePostImage(String imagePath) {
    try {
      Path path = uploadRoot.resolve(imagePath).normalize();
      if (path.startsWith(uploadRoot)) {
        Files.deleteIfExists(path);
      }
    } catch (IOException ignored) {
    }
  }

  private Page<PostDtos.PostResponse> hydrate(Page<Post> page, Long me) {
    List<Post> posts = page.getContent();
    if (posts.isEmpty()) {
      return page.map(p -> toDto(p, Map.of(), Map.of(), null));
    }

    List<Long> postIds = posts.stream().map(Post::getId).toList();
    ReactionCounts counts = loadCounts(postIds);
    Map<Long, ReactionType> my = loadMyReactions(postIds, me);

    return page.map(p -> toDto(p, counts.likes, counts.dislikes, my.get(p.getId())));
  }

  private PostDtos.PostResponse hydrateSingle(Post post, Long me) {
    ReactionCounts counts = loadCounts(List.of(post.getId()));
    ReactionType my = postReactionRepository.findByPostIdAndUserId(post.getId(), me).map(PostReaction::getType).orElse(null);
    return toDto(post, counts.likes, counts.dislikes, my);
  }

  private ReactionCounts loadCounts(Collection<Long> postIds) {
    Map<Long, Long> likes = new HashMap<>();
    Map<Long, Long> dislikes = new HashMap<>();

    for (Object[] row : postReactionRepository.countByPostIds(postIds)) {
      Long postId = (Long) row[0];
      ReactionType type = (ReactionType) row[1];
      long cnt = ((Number) row[2]).longValue();
      if (type == ReactionType.LIKE) likes.put(postId, cnt);
      if (type == ReactionType.DISLIKE) dislikes.put(postId, cnt);
    }
    return new ReactionCounts(likes, dislikes);
  }

  private Map<Long, ReactionType> loadMyReactions(Collection<Long> postIds, Long me) {
    if (postIds.isEmpty()) return Map.of();
    Map<Long, ReactionType> map = new HashMap<>();
    for (Object[] row : postReactionRepository.findPostIdAndTypeByPostIdInAndUserId(postIds, me)) {
      map.put((Long) row[0], (ReactionType) row[1]);
    }
    return map;
  }

  private PostDtos.PostResponse toDto(Post post, Map<Long, Long> likes, Map<Long, Long> dislikes, ReactionType my) {
    UserDtos.UserSummary author = UserDtos.UserSummary.builder()
        .id(post.getAuthor().getId())
        .username(post.getAuthor().getUsername())
        .build();

    PostDtos.DogSummary dog = null;
    if (post.getDog() != null) {
      dog = PostDtos.DogSummary.builder()
          .id(post.getDog().getId())
          .name(post.getDog().getName())
          .build();
    }

    long likeCount = likes.getOrDefault(post.getId(), 0L);
    long dislikeCount = dislikes.getOrDefault(post.getId(), 0L);

    String imageUrl = post.getImagePath() != null ? "/posts/" + post.getId() + "/image" : null;

    return PostDtos.PostResponse.builder()
        .id(post.getId())
        .author(author)
        .dog(dog)
        .caption(post.getCaption())
        .imageUrl(imageUrl)
        .createdAt(post.getCreatedAt() == null ? null : post.getCreatedAt().toString())
        .updatedAt(post.getUpdatedAt() == null ? null : post.getUpdatedAt().toString())
        .likesCount(likeCount)
        .dislikesCount(dislikeCount)
        .myReaction(my == null ? "NONE" : my.name())
        .build();
  }

  private record ReactionCounts(Map<Long, Long> likes, Map<Long, Long> dislikes) {}

  public record PostImageResult(Resource resource, String contentType) {}
}
