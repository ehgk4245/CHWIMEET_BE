package com.back.domain.member.repository;

import com.back.domain.member.entity.Member;
import com.back.global.queryDsl.CustomQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.back.domain.member.entity.QMember.member;

@Repository
public class MemberQueryRepository extends CustomQuerydslRepositorySupport {
    public MemberQueryRepository() { super(Member.class); }

    public long bulkBanMember(List<Long> memberIds) {
        long updatedCount = getQueryFactory()
                .update(member)
                .set(member.isBanned, true)
                .where(member.id.in(memberIds))
                .execute();

        getEntityManager().clear();

        return updatedCount;
    }
}
