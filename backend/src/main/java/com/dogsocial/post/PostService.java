package com.dogsocial.post;

import com.dogsocial.dog.Dog;
import com.dogsocial.dog.DogRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final DogRepository dogRepository;
  private final FollowRepository followRepository;
  private final PostReactionRepository postReactionRepository;

  public PostService(
      PostRepository postRepository,
      UserRepository userRepository,
      DogRepository dogRepository,
      FollowRepository followRepository,
      PostReactionRepository postReactionRepository
  ) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.dogRepository = dogRepository;
    this.followRepository = followRepository;
    this.postReactionRepository = postReactionRepository;
  }

  public Page<PostDtos.PostResponse> feed(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    List<Long> followed = followRepository.findFollowedIds(me);
    if (followed == null || followed.isEmpty()) {
      return hydrate(postRepository.findByAuthorId(me, pageable), me);
    }
    // Avoid empty IN() issues by ensuring non-empty followed list.
    return hydrate(postRepository.feed(me, followed, pageable), me);
  }

  /** All posts on the network (for Browse). */
  public Page<PostDtos.PostResponse> allPosts(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return hydrate(postRepository.findAllPostsOrderByCreatedAtDesc(pageable), me);
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
  public PostDtos.PostResponse create(PostDtos.CreatePostRequest req) {
    Long me = SecurityUtils.requireUserId();
    User author = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    Dog dog = null;
    if (req.getDogId() != null) {
      dog = dogRepository.findById(req.getDogId()).orElseThrow(() -> new NotFoundException("Dog not found"));
      if (!dog.getOwner().getId().equals(me)) {
        throw new ForbiddenException("You can only post as your own dog");
      }
    }

    Post post = Post.builder()
        .author(author)
        .dog(dog)
        .caption(req.getCaption())
        .build();
    post = postRepository.save(post);
    return hydrateSingle(post, me);
  }

  @Transactional(readOnly = false)
  public PostDtos.PostResponse update(Long postId, PostDtos.UpdatePostRequest req) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    if (!post.getAuthor().getId().equals(me)) {
      throw new ForbiddenException("Only the author can update this post");
    }
    post.setCaption(req.getCaption());
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
        .email(post.getAuthor().getEmail())
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

    return PostDtos.PostResponse.builder()
        .id(post.getId())
        .author(author)
        .dog(dog)
        .caption(post.getCaption())
        .createdAt(post.getCreatedAt() == null ? null : post.getCreatedAt().toString())
        .updatedAt(post.getUpdatedAt() == null ? null : post.getUpdatedAt().toString())
        .likesCount(likeCount)
        .dislikesCount(dislikeCount)
        .myReaction(my == null ? "NONE" : my.name())
        .build();
  }

  private record ReactionCounts(Map<Long, Long> likes, Map<Long, Long> dislikes) {}
}

