package com.dogsocial.comment;

import com.dogsocial.api.PageResponse;
import com.dogsocial.comment.dto.CommentDtos;
import com.dogsocial.reaction.dto.ReactionDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/api/posts/{postId}/comments")
  public PageResponse<CommentDtos.CommentResponse> list(
      @PathVariable Long postId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    Page<CommentDtos.CommentResponse> page = commentService.listForPost(postId, pageable);
    return PageResponse.of(page);
  }

  @PostMapping("/api/posts/{postId}/comments")
  public CommentDtos.CommentResponse create(
      @PathVariable Long postId,
      @Valid @RequestBody CommentDtos.CreateCommentRequest req
  ) {
    return commentService.create(postId, req);
  }

  @PutMapping("/api/comments/{commentId}")
  public CommentDtos.CommentResponse update(@PathVariable Long commentId, @Valid @RequestBody CommentDtos.UpdateCommentRequest req) {
    return commentService.update(commentId, req);
  }

  @DeleteMapping("/api/comments/{commentId}")
  public void delete(@PathVariable Long commentId) {
    commentService.delete(commentId);
  }

  @PutMapping("/api/comments/{commentId}/reaction")
  public CommentDtos.CommentResponse react(@PathVariable Long commentId, @Valid @RequestBody ReactionDtos.ReactionRequest req) {
    return commentService.react(commentId, req);
  }
}

