package com.liftkart.product.controller;

import com.liftkart.product.dto.request.CreateProductRequest;
import com.liftkart.product.dto.request.CreateReviewRequest;
import com.liftkart.product.dto.request.UpdateInventoryRequest;
import com.liftkart.product.dto.request.UpdateProductRequest;
import com.liftkart.product.dto.response.ApiResponse;
import com.liftkart.product.dto.response.ProductResponse;
import com.liftkart.product.dto.response.ReviewResponse;
import com.liftkart.product.service.ProductService;
import com.liftkart.product.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a new product (Vendor)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader("X-User-Id") UUID vendorId) {

        ProductResponse response = productService.createProduct(request, vendorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", response));
    }

    @GetMapping
    @Operation(summary = "Get all active products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.getAllProducts(pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products with filters")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Search results",
                productService.searchProducts(
                        q, categoryId, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID customerId) {

        return ResponseEntity.ok(ApiResponse.success("Product fetched",
                productService.getProductById(id, customerId)));
    }

    @GetMapping("/vendor/my-products")
    @Operation(summary = "Get all products for logged-in vendor")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts(
            @RequestHeader("X-User-Id") UUID vendorId) {

        return ResponseEntity.ok(ApiResponse.success("Vendor products fetched",
                productService.getVendorProducts(vendorId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product (Vendor)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader("X-User-Id") UUID vendorId) {

        return ResponseEntity.ok(ApiResponse.success("Product updated",
                productService.updateProduct(id, request, vendorId)));
    }

    @PutMapping("/{id}/inventory")
    @Operation(summary = "Update product inventory (Vendor)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateInventory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInventoryRequest request,
            @RequestHeader("X-User-Id") UUID vendorId) {

        return ResponseEntity.ok(ApiResponse.success("Inventory updated",
                productService.updateInventory(id, request, vendorId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (Vendor)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID vendorId) {

        productService.deleteProduct(id, vendorId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Add a review to a product (Customer)")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader("X-User-Id") UUID customerId) {

        ReviewResponse response = reviewService.createReview(id, request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added", response));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched",
                reviewService.getProductReviews(id, pageable)));
    }
}