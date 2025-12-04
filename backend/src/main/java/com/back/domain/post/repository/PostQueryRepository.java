package com.back.domain.post.repository;

import static com.back.domain.member.entity.QMember.*;
import static com.back.domain.post.entity.QPost.*;
import static com.back.domain.post.entity.QPostRegion.*;
import static com.back.domain.region.entity.QRegion.*;
import static com.back.domain.reservation.entity.QReservation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.back.domain.post.common.EmbeddingStatus;
import com.back.domain.post.dto.req.PostEmbeddingDto;
import com.back.domain.post.entity.Post;
import com.back.domain.reservation.common.ReservationStatus;
import com.back.domain.reservation.entity.Reservation;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;

@Repository
public class PostQueryRepository extends CustomQuerydslRepositorySupport {

	public PostQueryRepository() {
		super(Post.class);
	}

	public Page<Post> findFilteredPosts(String keyword, List<Long> categoryId, List<Long> regionIds,
		Pageable pageable) {
		return applyPagination(pageable, contentQuery -> contentQuery.selectFrom(post)
			.leftJoin(post.postRegions, postRegion)
			.leftJoin(postRegion.region, region)
			.join(post.author, member)
			.fetchJoin()
			.where(containsKeyword(keyword), inCategoryIds(categoryId), inRegionIds(regionIds),
				post.isBanned.isFalse()), countQuery -> countQuery.select(post.count())
			.from(post)
			.leftJoin(post.postRegions, postRegion)
			.where(containsKeyword(keyword), inCategoryIds(categoryId), inRegionIds(regionIds),
				post.isBanned.isFalse()));
	}

	private BooleanExpression containsKeyword(String keyword) {
		return keyword != null ? post.title.containsIgnoreCase(keyword) : null;
	}

	private BooleanExpression inCategoryIds(List<Long> categoryIds) {
		return (categoryIds == null || categoryIds.isEmpty()) ? null : post.category.id.in(categoryIds);
	}

	private BooleanExpression inRegionIds(List<Long> regionIds) {
		return (regionIds == null || regionIds.isEmpty()) ? null : postRegion.region.id.in(regionIds);
	}

	public Page<Post> findMyPost(Long memberId, Pageable pageable) {

		return applyPagination(pageable,
			contentQuery -> contentQuery.selectFrom(post).where(post.author.id.eq(memberId)),
			countQuery -> countQuery.select(post.count()).from(post).where(post.author.id.eq(memberId)));
	}

	public List<LocalDateTime> findReservedDatesFromToday(Long postId) {
		LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

		List<ReservationStatus> excludedStatuses = List.of(ReservationStatus.PENDING_APPROVAL,
			ReservationStatus.CANCELLED, ReservationStatus.REJECTED);

		List<Reservation> reservations = select(reservation).from(reservation)
			.where(reservation.post.id.eq(postId), reservation.reservationEndAt.goe(today),
				reservation.status.notIn(excludedStatuses))
			.fetch();

		Set<LocalDateTime> allReservedDates = new HashSet<>();

		for (Reservation r : reservations) {
			LocalDateTime start = r.getReservationStartAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
			LocalDateTime end = r.getReservationEndAt().withHour(0).withMinute(0).withSecond(0).withNano(0);

			LocalDateTime current = start;
			while (!current.isAfter(end)) {
				if (!current.isBefore(today)) {
					allReservedDates.add(current);
				}
				current = current.plusDays(1);
			}
		}

		return allReservedDates.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * ID 목록을 받아 해당 게시글들을 벌크 UPDATE를 통해 '제재(banned)' 상태로 변경합니다.
	 * @return 실제로 변경된 레코드(row) 수
	 */
	public long bulkBanPosts(List<Long> postIds) {
		long updatedCount = getQueryFactory().update(post)
			.set(post.isBanned, true)
			.where(post.id.in(postIds))
			.execute();

		getEntityManager().clear();

		return updatedCount;
	}

	public List<Post> findPostsToEmbedWithDetails(int limit) {
		return selectFrom(post).join(post.category)
			.fetchJoin()
			.join(post.author)
			.fetchJoin()
			.leftJoin(post.postRegions)
			.fetchJoin()
			.where(post.embeddingStatus.eq(EmbeddingStatus.WAIT))
			.orderBy(post.createdAt.asc())
			.limit(limit)
			.fetch();
	}

	/**
	 * WAIT -> PENDING으로 벌크 업데이트 + 버전 증가
	 */
	public long bulkUpdateStatusToPendingWithVersion(List<Long> postIds) {
		if (postIds == null || postIds.isEmpty()) {
			return 0;
		}

		long updatedCount = getQueryFactory().update(post)
			.set(post.embeddingStatus, EmbeddingStatus.PENDING)
			.set(post.embeddingVersion, post.embeddingVersion.add(1))
			.where(post.id.in(postIds), post.embeddingStatus.eq(EmbeddingStatus.WAIT))
			.execute();

		getEntityManager().clear();
		return updatedCount;
	}

	/**
	 * 실제로 선점한 게시글만 필터링 (버전 검증)
	 */
	public List<PostEmbeddingDto> verifyAcquiredPosts(List<PostEmbeddingDto> postDtos) {
		if (postDtos == null || postDtos.isEmpty()) {
			return List.of();
		}

		Map<Long, Long> expectedVersions = postDtos.stream()
			.collect(Collectors.toMap(PostEmbeddingDto::id, dto -> dto.embeddingVersion() + 1));

		List<Long> postIds = new ArrayList<>(expectedVersions.keySet());

		List<Tuple> results = getQueryFactory().select(post.id, post.embeddingVersion)
			.from(post)
			.where(post.id.in(postIds), post.embeddingStatus.eq(EmbeddingStatus.PENDING))
			.fetch();

		Set<Long> acquiredIds = results.stream().filter(tuple -> {
			Long id = tuple.get(post.id);
			Long currentVersion = tuple.get(post.embeddingVersion);
			return currentVersion.equals(expectedVersions.get(id));
		}).map(tuple -> tuple.get(post.id)).collect(Collectors.toSet());

		return postDtos.stream().filter(dto -> acquiredIds.contains(dto.id())).toList();
	}

	public long bulkUpdateStatus(List<Long> postIds, EmbeddingStatus toStatus, EmbeddingStatus fromStatus) {
		if (postIds == null || postIds.isEmpty()) {
			return 0;
		}

		long updatedCount = getQueryFactory().update(post)
			.set(post.embeddingStatus, toStatus)
			.where(post.id.in(postIds), post.embeddingStatus.eq(fromStatus))
			.execute();

		getEntityManager().clear();

		return updatedCount;
	}
}



