package com.dogsocial.playdate;

import com.dogsocial.api.PageResponse;
import com.dogsocial.playdate.dto.PlaydateDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playdates")
public class PlaydateController {
  private final PlaydateService playdateService;

  public PlaydateController(PlaydateService playdateService) {
    this.playdateService = playdateService;
  }

  @PostMapping
  public PlaydateDtos.PlaydateResponse create(@Valid @RequestBody PlaydateDtos.CreatePlaydateRequest req) {
    return playdateService.create(req);
  }

  @PutMapping("/{playdateId}/status")
  public PlaydateDtos.PlaydateResponse updateStatus(
      @PathVariable Long playdateId,
      @Valid @RequestBody PlaydateDtos.UpdateStatusRequest req
  ) {
    return playdateService.updateStatus(playdateId, req);
  }

  @GetMapping("/incoming")
  public PageResponse<PlaydateDtos.PlaydateResponse> incoming(
      @PageableDefault(size = 10, sort = "scheduledAt", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    Page<PlaydateDtos.PlaydateResponse> page = playdateService.incoming(pageable);
    return PageResponse.of(page);
  }

  @GetMapping("/upcoming")
  public PageResponse<PlaydateDtos.PlaydateResponse> upcoming(
      @PageableDefault(size = 10, sort = "scheduledAt", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    Page<PlaydateDtos.PlaydateResponse> page = playdateService.upcoming(pageable);
    return PageResponse.of(page);
  }

  @GetMapping("/past")
  public PageResponse<PlaydateDtos.PlaydateResponse> past(
      @PageableDefault(size = 10, sort = "scheduledAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<PlaydateDtos.PlaydateResponse> page = playdateService.past(pageable);
    return PageResponse.of(page);
  }
}

