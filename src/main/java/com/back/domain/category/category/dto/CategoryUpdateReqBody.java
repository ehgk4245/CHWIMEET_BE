package com.back.domain.category.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateReqBody(
        @NotBlank
        String name
) {
}
