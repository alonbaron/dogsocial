package com.dogsocial.playdate;

import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import com.dogsocial.playdate.dto.PlaydateDtos;
import com.dogsocial.security.SecurityUtils;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import com.dogsocial.user.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PlaydateService {
  private final PlaydateRepository playdateRepository;
  private final UserRepository userRepository;

  public PlaydateService(PlaydateRepository playdateRepository, UserRepository userRepository) {
    this.playdateRepository = playdateRepository;
    this.userRepository = userRepository;
  }

  public PlaydateDtos.PlaydateResponse create(PlaydateDtos.CreatePlaydateRequest req) {
    Long me = SecurityUtils.requireUserId();
    if (me.equals(req.getRecipientId())) {
      throw new BadRequestException("Cannot create a playdate with yourself");
    }
    if (req.getScheduledAt() == null || !req.getScheduledAt().isAfter(Instant.now())) {
      throw new BadRequestException("scheduledAt must be a future datetime");
    }

    User requester = userRepository.findById(me).orElseThrow(() -> new NotFoundException("User not found"));
    User recipient = userRepository.findById(req.getRecipientId()).orElseThrow(() -> new NotFoundException("Recipient not found"));

    Playdate p = Playdate.builder()
        .requester(requester)
        .recipient(recipient)
        .scheduledAt(req.getScheduledAt())
        .status(PlaydateStatus.PENDING)
        .note(req.getNote())
        .build();
    p = playdateRepository.save(p);
    return toDto(p, me);
  }

  public PlaydateDtos.PlaydateResponse updateStatus(Long playdateId, PlaydateDtos.UpdateStatusRequest req) {
    Long me = SecurityUtils.requireUserId();
    Playdate p = playdateRepository.findById(playdateId).orElseThrow(() -> new NotFoundException("Playdate not found"));

    PlaydateStatus desired = req.getStatus();
    if (desired == null || desired == PlaydateStatus.PENDING) {
      throw new BadRequestException("Invalid status change");
    }

    boolean isRequester = p.getRequester().getId().equals(me);
    boolean isRecipient = p.getRecipient().getId().equals(me);
    if (!isRequester && !isRecipient) {
      throw new ForbiddenException("Not part of this playdate");
    }

    if (desired == PlaydateStatus.APPROVED || desired == PlaydateStatus.DECLINED) {
      if (!isRecipient) {
        throw new ForbiddenException("Only the recipient can approve/decline");
      }
      if (p.getStatus() != PlaydateStatus.PENDING) {
        throw new BadRequestException("Only pending playdates can be approved/declined");
      }
      p.setStatus(desired);
    } else if (desired == PlaydateStatus.CANCELED) {
      if (p.getStatus() != PlaydateStatus.PENDING && p.getStatus() != PlaydateStatus.APPROVED) {
        throw new BadRequestException("Only pending/approved playdates can be canceled");
      }
      p.setStatus(PlaydateStatus.CANCELED);
    }

    p = playdateRepository.save(p);
    return toDto(p, me);
  }

  @Transactional(readOnly = true)
  public Page<PlaydateDtos.PlaydateResponse> incoming(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return playdateRepository.incoming(me, pageable).map(p -> toDto(p, me));
  }

  @Transactional(readOnly = true)
  public Page<PlaydateDtos.PlaydateResponse> upcoming(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return playdateRepository.upcoming(me, Instant.now(), pageable).map(p -> toDto(p, me));
  }

  @Transactional(readOnly = true)
  public Page<PlaydateDtos.PlaydateResponse> past(Pageable pageable) {
    Long me = SecurityUtils.requireUserId();
    return playdateRepository.past(me, Instant.now(), pageable).map(p -> toDto(p, me));
  }

  private PlaydateDtos.PlaydateResponse toDto(Playdate p, Long me) {
    UserDtos.UserSummary requester = UserDtos.UserSummary.builder()
        .id(p.getRequester().getId())
        .email(p.getRequester().getEmail())
        .build();
    UserDtos.UserSummary recipient = UserDtos.UserSummary.builder()
        .id(p.getRecipient().getId())
        .email(p.getRecipient().getEmail())
        .build();

    return PlaydateDtos.PlaydateResponse.builder()
        .id(p.getId())
        .requester(requester)
        .recipient(recipient)
        .scheduledAt(p.getScheduledAt())
        .status(p.getStatus())
        .note(p.getNote())
        .createdAt(p.getCreatedAt() == null ? null : p.getCreatedAt().toString())
        .updatedAt(p.getUpdatedAt() == null ? null : p.getUpdatedAt().toString())
        .isRequester(p.getRequester().getId().equals(me))
        .build();
  }
}

