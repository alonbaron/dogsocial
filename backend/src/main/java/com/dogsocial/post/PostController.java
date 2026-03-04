package com.dogsocial.post;

import com.dogsocial.api.PageResponse;
import com.dogsocial.post.dto.PostDtos;
import com.dogsocial.reaction.dto.ReactionDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {
  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @GetMapping("/api/feed")
  public PageResponse<PostDtos.PostResponse> feed(
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<PostDtos.PostResponse> page = postService.feed(pageable);
    return PageResponse.of(page);
  }

  @GetMapping("/api/browse/posts")
  public PageResponse<PostDtos.PostResponse> browsePosts(
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<PostDtos.PostResponse> page = postService.allPosts(pageable);
    return PageResponse.of(page);
  }

  @PostMapping("/api/posts")
  public PostDtos.PostResponse create(@Valid @RequestBody PostDtos.CreatePostRequest req) {
    return postService.create(req);
  }

  @GetMapping("/api/posts/{postId}")
  public PostDtos.PostResponse get(@PathVariable Long postId) {
    return postService.get(postId);
  }

  @PutMapping("/api/posts/{postId}")
  public PostDtos.PostResponse update(@PathVariable Long postId, @Valid @RequestBody PostDtos.UpdatePostRequest req) {
    return postService.update(postId, req);
  }

  @DeleteMapping("/api/posts/{postId}")
  public void delete(@PathVariable Long postId) {
    postService.delete(postId);
  }

  @GetMapping("/api/dogs/{dogId}/posts")
  public PageResponse<PostDtos.PostResponse> dogPosts(
      @PathVariable Long dogId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<PostDtos.PostResponse> page = postService.postsForDog(dogId, pageable);
    return PageResponse.of(page);
  }

  @PutMapping("/api/posts/{postId}/reaction")
  public PostDtos.PostResponse react(@PathVariable Long postId, @Valid @RequestBody ReactionDtos.ReactionRequest req) {
    return postService.react(postId, req);
  }
}

