package com.back.domain.post.repository;

import static com.back.domain.post.entity.QPost.*;
import static com.back.domain.post.entity.QPostFavorite.*;
import static com.back.domain.post.entity.QPostImage.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.back.domain.post.entity.PostFavorite;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;

@Repository
public class PostFavoriteQueryRepository extends CustomQuerydslRepositorySupport {
	public PostFavoriteQueryRepository() {
		super(PostFavorite.class);
	}

	public Page<PostFavorite> findFavoritePosts(long memberId, Pageable pageable) {
		return applyPagination(
			pageable,
			contentQuery -> contentQuery
				.selectFrom(postFavorite)
				.join(postFavorite.post, post).fetchJoin()
				.leftJoin(post.images, postImage).fetchJoin()
				.where(postFavorite.member.id
					.eq(memberId))
				.distinct(),

			countQuery -> countQuery
				.select(postFavorite.count())
				.from(postFavorite)
				.where(postFavorite.member.id.eq(memberId))

		);

	}

}
