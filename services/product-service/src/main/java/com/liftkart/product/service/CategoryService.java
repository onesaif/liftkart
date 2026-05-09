package com.liftkart.product.service;

import com.liftkart.product.dto.response.CategoryResponse;
import com.liftkart.product.entity.Category;
import com.liftkart.product.exception.ResourceNotFoundException;
import com.liftkart.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository
                .findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository
                .findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + slug));
        return mapToResponse(category);
    }

    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found"));
        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        List<CategoryResponse> children = category.getChildren() == null
                ? List.of()
                : category.getChildren().stream()
                  .filter(c -> !c.getIsDeleted())
                  .map(this::mapToResponse)
                  .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null
                        ? category.getParent().getId() : null)
                .iconUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .children(children)
                .build();
    }
}