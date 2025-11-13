package com.back.domain.post.dto.res;

import lombok.Builder;

@Builder
public record PostOptionResBody(
        String name,
        Integer deposit,
        Integer fee
) {}
