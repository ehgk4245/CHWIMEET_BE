package com.back.domain.reservation.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.back.domain.post.post.entity.QPostOption.postOption;
import static com.back.domain.reservation.entity.QReservation.reservation;
import static com.back.domain.reservation.entity.QReservationOption.reservationOption;
import static com.back.domain.member.member.entity.QMember.member;
import static com.back.domain.post.post.entity.QPost.post;
import static com.back.domain.post.post.entity.QPostImage.postImage;

@Repository
public class ReservationQueryRepository extends CustomQuerydslRepositorySupport
        implements ReservationRepositoryCustom {

    // 종료된 상태 목록
    private static final List<ReservationStatus> TERMINATED_STATUSES = List.of(
            ReservationStatus.CANCELLED,
            ReservationStatus.REJECTED,
            ReservationStatus.REFUND_COMPLETED,
            ReservationStatus.CLAIM_COMPLETED
    );

    public ReservationQueryRepository() {
        super(Reservation.class);
    }

    @Override
    public boolean existsOverlappingReservation(
            Long postId,
            LocalDate startAt,
            LocalDate endAt,
            Long excludeReservationId) {

        Long result = select(reservation.id)
                .from(reservation)
                .where(
                        postIdEq(postId),
                        excludeReservationIdNe(excludeReservationId),
                        statusNotIn(ReservationStatus.CANCELLED, ReservationStatus.REJECTED),
                        dateOverlap(startAt, endAt)
                )
                .fetchFirst();  // LIMIT 1

        return result != null;
    }

    @Override
    public boolean existsActiveReservation(Long postId, Long authorId) {
        Long result = select(reservation.id)
                .from(reservation)
                .where(
                        postIdEq(postId),
                        authorIdEq(authorId),
                        statusNotIn(TERMINATED_STATUSES.toArray(new ReservationStatus[0]))
                )
                .fetchFirst();  // LIMIT 1

        return result != null;
    }

    @Override
    public Optional<Reservation> findByIdWithOptions(Long id) {
        Reservation result = selectFrom(reservation)
                .leftJoin(reservation.reservationOptions, reservationOption).fetchJoin()
                .leftJoin(reservationOption.postOption, postOption).fetchJoin()
                .where(reservation.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Reservation> findByAuthorWithFetch(Member author, Pageable pageable) {
        List<Reservation> content = selectFrom(reservation)
                .leftJoin(reservation.post, post).fetchJoin()
                .leftJoin(post.author, member).fetchJoin()
                .leftJoin(post.images, postImage).fetchJoin()
                .leftJoin(reservation.reservationOptions, reservationOption).fetchJoin()
                .leftJoin(reservationOption.postOption, postOption).fetchJoin()
                .where(reservation.author.eq(author))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(reservation.id.desc())
                .fetch();

        Long total = select(reservation.count())
                .from(reservation)
                .where(reservation.author.eq(author))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Reservation> findByAuthorAndStatusWithFetch(
            Member author,
            ReservationStatus status,
            Pageable pageable) {
        List<Reservation> content = selectFrom(reservation)
                .leftJoin(reservation.post, post).fetchJoin()
                .leftJoin(post.author, member).fetchJoin()
                .leftJoin(post.images, postImage).fetchJoin()
                .leftJoin(reservation.reservationOptions, reservationOption).fetchJoin()
                .leftJoin(reservationOption.postOption, postOption).fetchJoin()
                .where(
                        reservation.author.eq(author),
                        reservation.status.eq(status)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(reservation.id.desc())
                .fetch();

        Long total = select(reservation.count())
                .from(reservation)
                .where(
                        reservation.author.eq(author),
                        reservation.status.eq(status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    // ===== 동적 조건 메서드 (Report 예시 스타일) =====

    private BooleanExpression postIdEq(Long postId) {
        return postId != null ? reservation.post.id.eq(postId) : null;
    }

    private BooleanExpression authorIdEq(Long authorId) {
        return authorId != null ? reservation.author.id.eq(authorId) : null;
    }

    private BooleanExpression excludeReservationIdNe(Long excludeReservationId) {
        return excludeReservationId != null
                ? reservation.id.ne(excludeReservationId)
                : null;
    }

    private BooleanExpression statusNotIn(ReservationStatus... statuses) {
        return statuses != null && statuses.length > 0
                ? reservation.status.notIn(statuses)
                : null;
    }

    private BooleanExpression dateOverlap(LocalDate startAt, LocalDate endAt) {
        if (startAt == null || endAt == null) {
            return null;
        }
        return reservation.reservationStartAt.lt(endAt)
                .and(reservation.reservationEndAt.goe(startAt));
    }
}