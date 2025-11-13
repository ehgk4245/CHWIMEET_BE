package com.back.domain.chat.chat.service;

import com.back.domain.chat.chat.dto.CreateChatRoomResBody;
import com.back.domain.chat.chat.entity.ChatRoom;
import com.back.domain.chat.chat.repository.ChatMemberRepository;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;

    public CreateChatRoomResBody createChatRoom(Long postId, Long memberId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 게시글입니다."));

        Member host = post.getAuthor();
        Member guest = memberService.getById(memberId);

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByPostAndMembers(postId, host.getId(), guest.getId());
        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            return new CreateChatRoomResBody("이미 존재하는 채팅방입니다.", room.getId());
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .build();

        chatRoom.addMember(host);
        chatRoom.addMember(guest);

        chatRoomRepository.save(chatRoom);

        return new CreateChatRoomResBody("채팅방이 생성되었습니다.", chatRoom.getId());
    }
}
