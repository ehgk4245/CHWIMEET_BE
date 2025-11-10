package com.back.domain.category.category.controller;

import com.back.domain.category.category.dto.CategoryReqBody;
import com.back.domain.category.category.dto.CategoryResBody;
import com.back.domain.category.category.service.CategoryService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/adm/categories")
public class CategoryAdmController {

    private final CategoryService categoryService;

    @PostMapping
    public RsData<CategoryResBody> createCategory(@RequestBody CategoryReqBody categoryReqBody) {
        CategoryResBody categoryResBody = categoryService.createCategory(categoryReqBody);
        return RsData.success("카테고리 등록 성공", categoryResBody);
    }
}
