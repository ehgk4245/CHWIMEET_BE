package com.back.domain.post.service;

import com.back.domain.post.dto.res.PostListResBody;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostFavoriteRepository;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final PostVectorService postVectorService;
    private final PostRepository postRepository;
    private final PostFavoriteRepository postfavoriteRepository;
    private final ChatClient chatClient;

    public List<PostListResBody> searchPosts(String query, Long memberId) {

        List<Long> postIds = postVectorService.searchPostIds(query, 10);

        if (postIds.isEmpty()) return List.of();

        List<Post> posts = postIds.stream()
                .map(id -> postRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (memberId != null) {

            return posts.stream()
                    .map(post -> {

                        boolean isFavorite = postfavoriteRepository.existsByMemberIdAndPostId(memberId, post.getId());

                        return PostListResBody.of(post, isFavorite);
                    })
                    .toList();
        }

        return posts.stream()
                .map(PostListResBody::of)
                .toList();
    }

    public String searchWithLLM(String query) {
        List<Document> docs = postVectorService.searchDocuments(query, 5);

        String context = docs.stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                질문:
                %s
                
                아래는 관련 게시글 정보들이야.
                이 정보들을 참고해서 자연스럽고 정확하게 답변해줘.
                
                -------------------------
                %s
                """.formatted(query, context);

        return chatClient.prompt(prompt)
                .options(ChatOptions.builder()
                        .temperature(1.0)
                        .build())
                .call()
                .content();
    }
}
