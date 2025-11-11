package com.back.domain.category.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateReqBody(
        Long parentId,

        @NotBlank
        String name
) {
}
