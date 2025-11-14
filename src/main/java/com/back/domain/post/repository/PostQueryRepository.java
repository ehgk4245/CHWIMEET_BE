package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;

public class PostQueryRepository extends CustomQuerydslRepositorySupport {

    public PostQueryRepository(){
        super(Post.class);
    }

}
