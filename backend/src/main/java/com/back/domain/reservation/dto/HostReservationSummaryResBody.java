package com.back.domain.reservation.dto;

import com.back.domain.reservation.common.ReservationDeliveryMethod;
import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public record HostReservationSummaryResBody(
        Long id,
        Long postId,
        AuthorDto author,
        ReservationStatus status,
        ReservationDeliveryMethod receiveMethod,
        ReservationDeliveryMethod returnMethod,
        String cancelReason,
        String rejectReason,
        LocalDateTime reservationStartAt,
        LocalDateTime reservationEndAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<OptionDto> option,
        int totalAmount
) {
    public HostReservationSummaryResBody(Reservation reservation, List<OptionDto> optionDtos, int calculatedTotalAmount, String profileImgUrl) {
        this(
                reservation.getId(),
                reservation.getPost().getId(),
                new AuthorDto(reservation.getAuthor(), profileImgUrl),
                reservation.getStatus(),
                reservation.getReceiveMethod(),
                reservation.getReturnMethod(),
                reservation.getCancelReason(),
                reservation.getRejectReason(),
                reservation.getReservationStartAt(),
                reservation.getReservationEndAt(),
                reservation.getCreatedAt(),
                reservation.getModifiedAt(),
                optionDtos,
                calculatedTotalAmount
        );
    }
}
