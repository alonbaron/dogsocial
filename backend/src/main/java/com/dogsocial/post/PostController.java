package com.dogsocial.post;

import com.dogsocial.api.PageResponse;
import com.dogsocial.post.dto.PostDtos;
import com.dogsocial.reaction.dto.ReactionDtos;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

  @PostMapping(value = "/api/posts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public PostDtos.PostResponse create(
      @RequestParam("caption") String caption,
      @RequestParam(value = "dogId", required = false) Long dogId,
      @RequestParam(value = "image", required = false) MultipartFile image
  ) {
    return postService.create(caption, dogId, image);
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

  @GetMapping("/api/users/{userId}/posts")
  public PageResponse<PostDtos.PostResponse> userPosts(
      @PathVariable Long userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<PostDtos.PostResponse> page = postService.postsForUser(userId, pageable);
    return PageResponse.of(page);
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

  @GetMapping("/api/posts/{postId}/image")
  public ResponseEntity<Resource> getImage(@PathVariable Long postId) {
    PostService.PostImageResult result = postService.getImage(postId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(result.contentType()))
        .body(result.resource());
  }
}
