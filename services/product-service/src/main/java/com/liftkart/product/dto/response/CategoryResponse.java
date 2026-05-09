package com.liftkart.product.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private String iconUrl;
    private Integer displayOrder;
    private List<CategoryResponse> children;
}