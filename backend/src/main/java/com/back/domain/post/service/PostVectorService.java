package com.back.domain.post.service;

import com.back.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostVectorService {

    private final VectorStore vectorStore;

    public void indexPost(Post post) {

        String text = post.getTitle() + "\n" + post.getContent();

        String docId = UUID.randomUUID().toString();

        vectorStore.add(List.of(
                new Document(
                        docId,
                        text,
                        Map.of("postId", post.getId())
                )
        ));

    }

    public List<Long> searchPostIds(String query, int topK) {

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        return docs.stream()
                .map(doc -> (Number) doc.getMetadata().get("postId"))
                .map(Number::longValue)
                .toList();
    }
    public List<Document> searchDocuments(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        return vectorStore.similaritySearch(request);

    }
}
