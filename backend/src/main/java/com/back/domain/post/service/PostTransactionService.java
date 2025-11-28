package com.back.domain.post.service;

import com.back.domain.post.common.EmbeddingStatus;
import com.back.domain.post.repository.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostTransactionService {
    private final PostQueryRepository postQueryRepository;

    /**
     * WAIT -> PENDING으로 벌크 업데이트 (선점)
     */
    @Transactional
    public long updateStatusToPending(List<Long> postIds) {
        return postQueryRepository.bulkUpdateStatus(
                postIds,
                EmbeddingStatus.PENDING,
                EmbeddingStatus.WAIT
        );
    }

    /**
     * PENDING -> DONE으로 벌크 업데이트 (완료)
     */
    @Transactional
    public void updateStatusToDone(List<Long> postIds) {
        postQueryRepository.bulkUpdateStatus(
                postIds,
                EmbeddingStatus.DONE,
                EmbeddingStatus.PENDING
        );
    }

    /**
     * PENDING -> WAIT으로 벌크 업데이트 (실패 복구)
     */
    @Transactional
    public void updateStatusToWait(List<Long> postIds) {
        postQueryRepository.bulkUpdateStatus(
                postIds,
                EmbeddingStatus.WAIT,
                EmbeddingStatus.PENDING
        );
    }
}
