package com.back.domain.post.entity;

import java.util.ArrayList;
import java.util.List;

import com.back.domain.category.entity.Category;
import com.back.domain.member.entity.Member;
import com.back.domain.post.common.EmbeddingStatus;
import com.back.domain.post.common.ReceiveMethod;
import com.back.domain.post.common.ReturnMethod;
import com.back.global.jpa.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {
	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "receive_method", nullable = false)
	private ReceiveMethod receiveMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "return_method", nullable = false)
	private ReturnMethod returnMethod;

	@Column(name = "return_address1")
	private String returnAddress1;

	@Column(name = "return_address2")
	private String returnAddress2;

	@Column(name = "deposit", nullable = false)
	private Integer deposit;

	@Column(name = "fee", nullable = false)
	private Integer fee;

	@Column(name = "is_banned", nullable = false)
	private Boolean isBanned = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "embedding_status", nullable = false)
	private EmbeddingStatus embeddingStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostOption> options = new ArrayList<>();

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostRegion> postRegions = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "category_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_post_category")
	)
	private Category category;

	@Version
	@Column(name = "embedding_version", nullable = false)
	private Long embeddingVersion;

	public static Post of(
		String title,
		String content,
		ReceiveMethod receiveMethod,
		ReturnMethod returnMethod,
		String returnAddress1,
		String returnAddress2,
		Integer deposit,
		Integer fee,
		Member author,
		Category category
	) {
		Post post = new Post();
		post.title = title;
		post.content = content;
		post.receiveMethod = receiveMethod;
		post.returnMethod = returnMethod;
		post.returnAddress1 = returnAddress1;
		post.returnAddress2 = returnAddress2;
		post.deposit = deposit;
		post.fee = fee;
		post.author = author;
		post.category = category;
		post.isBanned = false;
		post.embeddingStatus = EmbeddingStatus.WAIT;
		post.embeddingVersion = 0L;
		return post;
	}

	public void updatePost(
		String title,
		String content,
		ReceiveMethod receiveMethod,
		ReturnMethod returnMethod,
		String returnAddress1,
		String returnAddress2,
		Integer deposit,
		Integer fee
	) {
		this.title = title;
		this.content = content;
		this.receiveMethod = receiveMethod;
		this.returnMethod = returnMethod;
		this.returnAddress1 = returnAddress1;
		this.returnAddress2 = returnAddress2;
		this.deposit = deposit;
		this.fee = fee;
		this.embeddingStatus = EmbeddingStatus.WAIT;
	}

	public void updateCategory(Category category) {
		this.category = category;
	}

	public void resetPostRegions(List<PostRegion> newPostRegions) {
		this.postRegions.clear();
		newPostRegions.forEach(postRegion -> postRegion.setPost(this));
		this.postRegions.addAll(newPostRegions);
	}

	public void resetPostImages(List<PostImage> newPostImages) {
		this.images.clear();
		newPostImages.forEach(postImage -> postImage.setPost(this));
		this.images.addAll(newPostImages);
	}

	public void resetPostOptions(List<PostOption> newPostOptions) {
		this.options.clear();
		newPostOptions.forEach(postOption -> postOption.setPost(this));
		this.options.addAll(newPostOptions);
	}

	public void ban() {
		this.isBanned = true;
	}

	public void unban() {
		this.isBanned = false;
	}

	public void updateEmbeddingStatusWait() {
		this.embeddingStatus = EmbeddingStatus.WAIT;
	}
}