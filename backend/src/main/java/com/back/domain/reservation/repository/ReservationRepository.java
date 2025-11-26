package com.back.domain.reservation.repository;

import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
//    Page<Reservation> findByPost(Post post, Pageable pageable);
//    Page<Reservation> findByPostAndStatus(Post post, ReservationStatus status, Pageable pageable);
    List<Reservation> findByStatus(ReservationStatus statuses);
}
