package com.dogsocial.comment;

import com.dogsocial.comment.dto.CommentDtos;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.post.Post;
import com.dogsocial.post.PostRepository;
import com.dogsocial.reaction.CommentReaction;
import com.dogsocial.reaction.CommentReactionRepository;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CommentService {
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentReactionRepository commentReactionRepository;

  public CommentService(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository,
      CommentReactionRepository commentReactionRepository
  ) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.commentReactionRepository = commentReactionRepository;
  }

  public Page<CommentDtos.CommentResponse> listForPost(Long postId, Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

    Page<Comment> page = commentRepository.findByPostId(post.getId(), pageable);
    if (page.isEmpty()) {
      return page.map(c -> toDto(c, Map.of(), Map.of(), null));
    }

    Collection<Long> commentIds = page.getContent().stream().map(Comment::getId).toList();
    ReactionCounts counts = loadCounts(commentIds);
    Map<Long, ReactionType> my = loadMyReactions(commentIds, me);

    return page.map(c -> toDto(c, counts.likes, counts.dislikes, my.get(c.getId())));
  }

  @Transactional(readOnly = false)
  public CommentDtos.CommentResponse create(Long postId, CommentDtos.CreateCommentRequest req) {
    Long me = SecurityUtils.requireUserId();
    Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
    User author = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    Comment comment = Comment.builder()
        .post(post)
        .author(author)
        .content(req.getContent())
        .build();
    comment = commentRepository.save(comment);
    return toDto(comment, Map.of(), Map.of(), null);
  }

  @Transactional(readOnly = false)
  public CommentDtos.CommentResponse update(Long commentId, CommentDtos.UpdateCommentRequest req) {
    Long me = SecurityUtils.requireUserId();
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
    if (!comment.getAuthor().getId().equals(me)) {
      throw new ForbiddenException("Only the author can update this comment");
    }
    comment.setContent(req.getContent());
    comment = commentRepository.save(comment);

    ReactionCounts counts = loadCounts(java.util.List.of(comment.getId()));
    ReactionType my = commentReactionRepository.findByCommentIdAndUserId(comment.getId(), me).map(CommentReaction::getType).orElse(null);
    return toDto(comment, counts.likes, counts.dislikes, my);
  }

  @Transactional(readOnly = false)
  public void delete(Long commentId) {
    Long me = SecurityUtils.requireUserId();
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
    if (!comment.getAuthor().getId().equals(me)) {
      throw new ForbiddenException("Only the author can delete this comment");
    }
    commentRepository.delete(comment);
  }

  @Transactional(readOnly = false)
  public CommentDtos.CommentResponse react(Long commentId, ReactionDtos.ReactionRequest req) {
    Long me = SecurityUtils.requireUserId();
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));

    ReactionDtos.ReactionSelection selection = req.getType();
    if (selection == ReactionDtos.ReactionSelection.NONE) {
      commentReactionRepository.deleteByCommentIdAndUserId(comment.getId(), me);
      commentReactionRepository.flush();
      ReactionCounts counts = loadCounts(java.util.List.of(comment.getId()));
      return toDto(comment, counts.likes, counts.dislikes, null);
    }

    ReactionType type = ReactionType.valueOf(selection.name());
    User user = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));

    CommentReaction reaction = commentReactionRepository.findByCommentIdAndUserId(comment.getId(), me)
        .orElse(CommentReaction.builder().comment(comment).user(user).build());
    reaction.setType(type);
    commentReactionRepository.save(reaction);

    ReactionCounts counts = loadCounts(java.util.List.of(comment.getId()));
    return toDto(comment, counts.likes, counts.dislikes, type);
  }

  private ReactionCounts loadCounts(Collection<Long> commentIds) {
    Map<Long, Long> likes = new HashMap<>();
    Map<Long, Long> dislikes = new HashMap<>();

    for (Object[] row : commentReactionRepository.countByCommentIds(commentIds)) {
      Long commentId = (Long) row[0];
      ReactionType type = (ReactionType) row[1];
      long cnt = ((Number) row[2]).longValue();
      if (type == ReactionType.LIKE) likes.put(commentId, cnt);
      if (type == ReactionType.DISLIKE) dislikes.put(commentId, cnt);
    }
    return new ReactionCounts(likes, dislikes);
  }

  private Map<Long, ReactionType> loadMyReactions(Collection<Long> commentIds, Long me) {
    if (commentIds.isEmpty()) return Map.of();
    Map<Long, ReactionType> map = new HashMap<>();
    for (Object[] row : commentReactionRepository.findCommentIdAndTypeByCommentIdInAndUserId(commentIds, me)) {
      map.put((Long) row[0], (ReactionType) row[1]);
    }
    return map;
  }

  private CommentDtos.CommentResponse toDto(Comment c, Map<Long, Long> likes, Map<Long, Long> dislikes, ReactionType my) {
    UserDtos.UserSummary author = UserDtos.UserSummary.builder()
        .id(c.getAuthor().getId())
        .username(c.getAuthor().getUsername())
        .build();

    return CommentDtos.CommentResponse.builder()
        .id(c.getId())
        .postId(c.getPost().getId())
        .author(author)
        .content(c.getContent())
        .createdAt(c.getCreatedAt() == null ? null : c.getCreatedAt().toString())
        .updatedAt(c.getUpdatedAt() == null ? null : c.getUpdatedAt().toString())
        .likesCount(likes.getOrDefault(c.getId(), 0L))
        .dislikesCount(dislikes.getOrDefault(c.getId(), 0L))
        .myReaction(my == null ? "NONE" : my.name())
        .build();
  }

  private record ReactionCounts(Map<Long, Long> likes, Map<Long, Long> dislikes) {}
}

