package com.back.domain.reservation.reservation.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.domain.reservation.reservation.common.ReservationStatus;
import com.back.domain.reservation.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // TODO: QueryDSL로 변경 예정
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.post.id = :postId
        AND r.status NOT IN ('CANCELLED', 'REJECTED')
        AND (r.reservationStartAt < :endAt AND r.reservationEndAt >= :startAt)
        """)
    boolean existsOverlappingReservation(@Param("postId") Long postId,
                                         @Param("startAt") LocalDate startAt,
                                         @Param("endAt") LocalDate endAt);
    boolean existsByPostIdAndAuthorId(Long postId, Long authorId);

    Page<Reservation> findByAuthor(Member author, Pageable pageable);
    Page<Reservation> findByAuthorAndStatus(Member author, ReservationStatus status, Pageable pageable);

    Page<Reservation> findByPost(Post post, Pageable pageable);
    Page<Reservation> findByPostAndStatus(Post post, ReservationStatus status, Pageable pageable);
}
