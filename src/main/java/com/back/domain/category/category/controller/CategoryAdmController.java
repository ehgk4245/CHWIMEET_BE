package com.back.domain.category.category.controller;

import com.back.domain.category.category.dto.CategoryCreateReqBody;
import com.back.domain.category.category.dto.CategoryResBody;
import com.back.domain.category.category.dto.CategoryUpdateReqBody;
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
    public RsData<CategoryResBody> createCategory(@RequestBody CategoryCreateReqBody categoryCreateReqBody) {
        CategoryResBody categoryResBody = categoryService.createCategory(categoryCreateReqBody);
        return RsData.success("카테고리 등록 성공", categoryResBody);
    }

    @PatchMapping("/{id}")
    public RsData<CategoryResBody> updateCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody CategoryUpdateReqBody categoryUpdateReqBody) {
        CategoryResBody categoryResBody = categoryService.updateCategory(categoryId, categoryUpdateReqBody);
        return RsData.success("카테고리 수정 성공", categoryResBody);
    }

    @DeleteMapping("/{id}")
    public RsData<Void> deleteCategory(@PathVariable("id") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return RsData.success("카테고리 삭제 성공");
    }
}
