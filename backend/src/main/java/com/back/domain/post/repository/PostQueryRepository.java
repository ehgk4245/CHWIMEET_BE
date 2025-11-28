package com.back.domain.post.repository;

import static com.back.domain.post.entity.QPost.*;
import static com.back.domain.post.entity.QPostRegion.*;
import static com.back.domain.region.entity.QRegion.*;
import static com.back.domain.reservation.entity.QReservation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.back.domain.post.entity.Post;
import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;

@Repository
public class PostQueryRepository extends CustomQuerydslRepositorySupport {

	public PostQueryRepository() {
		super(Post.class);
	}

	public Page<Post> findFilteredPosts(
		String keyword,
		List<Long> categoryId,
		List<Long> regionIds,
		Pageable pageable) {
		return applyPagination(
			pageable,
			contentQuery -> contentQuery
				.selectFrom(post).leftJoin(post.postRegions, postRegion).fetchJoin()
				.leftJoin(postRegion.region, region).fetchJoin()
				.where(
					containsKeyword(keyword),
					inCategoryIds(categoryId),
					inRegionIds(regionIds),
					post.isBanned.isFalse() // 제재 처리 된 게시물 제외
				)
				.distinct(),
			countQuery -> countQuery
				.select(post.count())
				.from(post)
				.leftJoin(post.postRegions, postRegion)
				.where(
					containsKeyword(keyword),
					inCategoryIds(categoryId),
					inRegionIds(regionIds
					)

				)
		);
	}

	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null ? post.title.containsIgnoreCase(keyword) : null;
	}

	private BooleanExpression inCategoryIds(List<Long> categoryIds) {
		return (categoryIds == null || categoryIds.isEmpty())
			? null
			: post.category.id.in(categoryIds);
	}

	private BooleanExpression inRegionIds(List<Long> regionIds) {
		return (regionIds == null || regionIds.isEmpty())
			? null
			: postRegion.region.id.in(regionIds);
	}

	public Page<Post> findMyPost(Long memberId, Pageable pageable) {

		return applyPagination(
			pageable,
			contentQuery -> contentQuery
				.selectFrom(post)
				.where(post.author.id.eq(memberId)),
			countQuery -> countQuery
				.select(post.count())
				.from(post)
				.where(post.author.id.eq(memberId))
		);
	}

	public List<LocalDateTime> findReservedDatesFromToday(Long postId) {
		LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

		List<ReservationStatus> excludedStatuses = List.of(
			ReservationStatus.PENDING_APPROVAL,
			ReservationStatus.CANCELLED,
			ReservationStatus.REJECTED
		);

		// 시작일과 종료일을 함께 조회
		List<Reservation> reservations = select(reservation)
			.from(reservation)
			.where(
				reservation.post.id.eq(postId),
				reservation.reservationEndAt.goe(today), // 종료일이 오늘 이후
				reservation.status.notIn(excludedStatuses)
			)
			.fetch();

		// 각 예약의 시작일~종료일 사이 모든 날짜를 Set에 담기 (중복 제거)
		Set<LocalDateTime> allReservedDates = new HashSet<>();

		for (Reservation r : reservations) {
			LocalDateTime start = r.getReservationStartAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
			LocalDateTime end = r.getReservationEndAt().withHour(0).withMinute(0).withSecond(0).withNano(0);

			// start부터 end까지 하루씩 증가하며 모든 날짜 추가
			LocalDateTime current = start;
			while (!current.isAfter(end)) {
				if (!current.isBefore(today)) { // 오늘 이후만
					allReservedDates.add(current);
				}
				current = current.plusDays(1);
			}
		}

		// 정렬해서 반환
		return allReservedDates.stream()
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * ID 목록을 받아 해당 게시글들을 벌크 UPDATE를 통해 '제재(banned)' 상태로 변경합니다.
	 * @return 실제로 변경된 레코드(row) 수
	 */
	public long bulkBanPosts(List<Long> postIds) {
		long updatedCount = getQueryFactory()
				.update(post) // UPDATE Post p
				.set(post.isBanned, true) // SET p.isBanned = true
				.where(post.id.in(postIds)) // WHERE p.id IN (:postIds)
				.execute(); // 쿼리 실행 및 변경된 행 개수 반환

		// 필요에 따라 영속성 컨텍스트(JPA 1차 캐시) 초기화
		// 벌크 연산은 캐시를 우회하므로, 이후 트랜잭션 내에서 최신 데이터를
		// 조회해야 한다면 반드시 초기화해야 합니다.
		getEntityManager().clear();

		return updatedCount;
	}
}



