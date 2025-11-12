package com.back.domain.reservation.reservation.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.reservation.reservation.common.ReservationStatus;
import com.back.domain.reservation.reservation.dto.CreateReservationReqBody;
import com.back.domain.reservation.reservation.dto.GuestReservationSummaryResBody;
import com.back.domain.reservation.reservation.dto.HostReservationSummaryResBody;
import com.back.domain.reservation.reservation.dto.ReservationDto;
import com.back.domain.reservation.reservation.entity.Reservation;
import com.back.domain.reservation.reservation.service.ReservationService;
import com.back.global.security.SecurityUser;
import com.back.standard.util.page.PagePayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final MemberService memberService;

    @Transactional
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody CreateReservationReqBody ReqBody,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Member author = memberService.getById(securityUser.getId());

        Reservation reservation = reservationService.create(ReqBody, author);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @Transactional(readOnly = true)
    @GetMapping("/sent")
    public ResponseEntity<PagePayload<GuestReservationSummaryResBody>> getSentReservations(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PageableDefault(size = 5, page = 0)Pageable pageable,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String keyword
            ) {
        Member author = memberService.getById(securityUser.getId());

        PagePayload<GuestReservationSummaryResBody> reservations = reservationService.getSentReservations(author, pageable, status, keyword);

        return ResponseEntity.ok(reservations);
    }

    @Transactional(readOnly = true)
    @GetMapping("/received/{postId}")
    public ResponseEntity<PagePayload<HostReservationSummaryResBody>> getReceivedReservations(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long postId,
            @PageableDefault(size = 5, page = 0)Pageable pageable,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String keyword
    ) {
        Member member = memberService.getById(securityUser.getId());
        PagePayload<HostReservationSummaryResBody> reservations = reservationService.getReceivedReservations(postId, member, pageable, status, keyword);

        return ResponseEntity.ok(reservations);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDto> getReservationDetail(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal SecurityUser securityUser
            ) {
        // TODO: logs 정보 가져오기 (service 에서 ReservationDto를 만들어 오는 방식 고려)
        ReservationDto reservationDto = reservationService.getReservationDtoById(reservationId, securityUser.getId());
        return ResponseEntity.ok(reservationDto);
    }
}
