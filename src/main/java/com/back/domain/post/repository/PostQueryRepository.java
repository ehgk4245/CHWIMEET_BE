package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.back.domain.post.entity.QPost.post;
import static com.back.domain.post.entity.QPostRegion.postRegion;
import static com.back.domain.region.entity.QRegion.region;

@Repository
public class PostQueryRepository extends CustomQuerydslRepositorySupport {

    public PostQueryRepository(){
        super(Post.class);
    }

    public Page<Post> findFilteredPosts(
            String keyword,
            Long categoryId,
            List<Long> regionIds,
            Pageable pageable) {
        return applyPagination(
                pageable,
                contentQuery -> contentQuery
                        .selectFrom(post).leftJoin(post.postRegions, postRegion).fetchJoin()
                        .leftJoin(postRegion.region, region).fetchJoin()
                        .where(
                                containsKeyword(keyword),
                                equalsCategoryId(categoryId),
                                inRegionIds(regionIds)
                        )
                        .distinct(),
                countQuery -> countQuery
                        .select(post.count())
                        .from(post)
                        .leftJoin(post.postRegions, postRegion)
                        .where(
                                containsKeyword(keyword),
                                equalsCategoryId(categoryId),
                                inRegionIds(regionIds
                                )

                        )
        );
    }

    private BooleanExpression containsKeyword(String keyword) {
        return keyword != null ? post.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression equalsCategoryId(Long categoryId) {
        return categoryId != null ? post.category.id.eq(categoryId) : null;
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
}



