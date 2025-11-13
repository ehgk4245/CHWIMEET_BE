package com.back.domain.reservation.repository;

import com.back.domain.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationRepositoryCustom {
    boolean existsOverlappingReservation(
            Long postId,
            LocalDate startAt,
            LocalDate endAt,
            Long excludeReservationId
    );

    boolean existsActiveReservation(Long postId, Long authorId);

    Optional<Reservation> findByIdWithOptions(Long id);
    Optional<Reservation> findByIdWithPostAndAuthor(Long id);

    Page<Reservation> findByAuthorWithFetch(Member author,
                                            ReservationStatus status,
                                            String keyword,
                                            Pageable pageable);

    Page<Reservation> findByPostWithFetch(Post post,
                                          ReservationStatus status,
                                          Pageable pageable);
}
