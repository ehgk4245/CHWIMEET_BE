package com.back.domain.post.scheduler.job;

import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PostEmbeddingJob implements Job {
    @Autowired
    private PostService postService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            postService.embedPostsBatch();
            log.info("게시글 임베딩 작업이 실행되었습니다.");
        } catch (Exception e) {
            log.error("게시글 임베딩 작업 중 오류 발생", e);
            throw new JobExecutionException(e);
        }
    }
}
