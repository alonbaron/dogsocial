package com.dogsocial.playdate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface PlaydateRepository extends JpaRepository<Playdate, Long> {
  @EntityGraph(attributePaths = {"requester", "recipient"})
  @Query("""
      select p from Playdate p
      where p.recipient.id = :me and p.status = com.dogsocial.playdate.PlaydateStatus.PENDING
      order by p.scheduledAt asc
      """)
  Page<Playdate> incoming(@Param("me") Long me, Pageable pageable);

  @EntityGraph(attributePaths = {"requester", "recipient"})
  @Query("""
      select p from Playdate p
      where (p.requester.id = :me or p.recipient.id = :me)
        and p.status = com.dogsocial.playdate.PlaydateStatus.APPROVED
        and p.scheduledAt >= :now
      order by p.scheduledAt asc
      """)
  Page<Playdate> upcoming(@Param("me") Long me, @Param("now") Instant now, Pageable pageable);

  @EntityGraph(attributePaths = {"requester", "recipient"})
  @Query("""
      select p from Playdate p
      where (p.requester.id = :me or p.recipient.id = :me)
        and p.status = com.dogsocial.playdate.PlaydateStatus.APPROVED
        and p.scheduledAt < :now
      order by p.scheduledAt desc
      """)
  Page<Playdate> past(@Param("me") Long me, @Param("now") Instant now, Pageable pageable);
}

