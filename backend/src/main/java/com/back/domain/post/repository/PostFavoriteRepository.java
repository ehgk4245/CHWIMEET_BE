package com.back.domain.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.back.domain.post.entity.PostFavorite;

@Repository
public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
	Optional<PostFavorite> findByMemberIdAndPostId(Long memberId, Long PostId);

	boolean existsByMemberIdAndPostId(Long memberId, Long postId);

}
