package com.back.domain.category.category.dto;

public record CategoryReqBody(
        Long parentId,
        String name
) {
}
