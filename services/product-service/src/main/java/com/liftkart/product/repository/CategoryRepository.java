package com.liftkart.product.repository;

import com.liftkart.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAsc();

    List<Category> findByParentIdAndIsDeletedFalse(UUID parentId);

    Optional<Category> findBySlugAndIsDeletedFalse(String slug);

    boolean existsBySlug(String slug);
}