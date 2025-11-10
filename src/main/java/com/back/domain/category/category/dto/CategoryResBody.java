package com.back.domain.category.category.dto;

import java.util.List;

public record CategoryResBody(
        String name,
        List<String> child
) {
}
