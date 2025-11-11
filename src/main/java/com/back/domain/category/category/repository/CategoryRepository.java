package com.back.domain.category.category.repository;

import com.back.domain.category.category.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = {"children"})
    Optional<Category> findCategoryWithChildById(Long id);
}
