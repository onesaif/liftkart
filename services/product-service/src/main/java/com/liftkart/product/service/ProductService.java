package com.liftkart.product.service;

import com.liftkart.product.dto.request.CreateProductRequest;
import com.liftkart.product.dto.request.UpdateInventoryRequest;
import com.liftkart.product.dto.request.UpdateProductRequest;
import com.liftkart.product.dto.response.ProductResponse;
import com.liftkart.product.entity.*;
import com.liftkart.product.exception.BadRequestException;
import com.liftkart.product.exception.ResourceNotFoundException;
import com.liftkart.product.repository.*;
import com.liftkart.product.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SlugGenerator slugGenerator;

    @Value("${app.rabbitmq.exchanges.product}")
    private String productExchange;

    @Value("${app.rabbitmq.routing-keys.low-stock}")
    private String lowStockRoutingKey;

    @Value("${app.kafka.topics.product-views}")
    private String productViewsTopic;

    // ── Create Product ─────────────────────────────────────────────
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UUID vendorId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String slug = slugGenerator.generate(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slugGenerator.generateUnique(request.getName());
        }

        Product product = Product.builder()
                .vendorId(vendorId)
                .category(category)
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .price(request.getPrice())
                .comparePrice(request.getComparePrice())
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .sku(request.getSku())
                .status("ACTIVE")
                .build();

        product.setCreatedBy(vendorId);
        product = productRepository.save(product);

        // Save images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            final Product savedProduct = product;
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(savedProduct)
                            .imageUrl(url)
                            .isPrimary(request.getImageUrls().indexOf(url) == 0)
                            .displayOrder(request.getImageUrls().indexOf(url))
                            .build())
                    .collect(Collectors.toList());
            savedProduct.setImages(images);
        }

        // Log initial inventory
        logInventory(product, request.getStockQuantity(),
                "RESTOCK", null, request.getStockQuantity());

        log.info("Product created: {} by vendor: {}", product.getId(), vendorId);
        return mapToResponse(product);
    }

    // ── Get Product by ID ──────────────────────────────────────────
    public ProductResponse getProductById(UUID id, UUID customerId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Publish view event to Kafka
        publishViewEvent(id, customerId);

        return mapToResponse(product);
    }

    // ── Get All Active Products ────────────────────────────────────
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository
                .findByStatusAndIsDeletedFalse("ACTIVE", pageable)
                .map(this::mapToResponse);
    }

    // ── Search Products ────────────────────────────────────────────
    public Page<ProductResponse> searchProducts(
            String keyword, UUID categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {

        return productRepository
                .searchProducts(keyword, categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    // ── Get Vendor Products ────────────────────────────────────────
    public List<ProductResponse> getVendorProducts(UUID vendorId) {
        return productRepository.findByVendorIdAndIsDeletedFalse(vendorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Update Product ─────────────────────────────────────────────
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request,
                                         UUID vendorId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {
            throw new BadRequestException("You can only update your own products");
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getComparePrice() != null)
            product.setComparePrice(request.getComparePrice());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getLowStockThreshold() != null)
            product.setLowStockThreshold(request.getLowStockThreshold());

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    // ── Update Inventory ───────────────────────────────────────────
    @Transactional
    public ProductResponse updateInventory(UUID id, UpdateInventoryRequest request,
                                           UUID vendorId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {
            throw new BadRequestException("You can only update your own products");
        }

        int newStock = product.getStockQuantity() + request.getQuantity();
        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock");
        }

        product.setStockQuantity(newStock);
        logInventory(product, request.getQuantity(),
                request.getReason(), null, newStock);

        // Alert if low stock
        if (newStock <= product.getLowStockThreshold()) {
            rabbitTemplate.convertAndSend(productExchange,
                    lowStockRoutingKey,
                    "Low stock alert for product: " + product.getId() +
                            " | Stock: " + newStock);
            log.warn("Low stock alert sent for product: {}", product.getId());
        }

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    // ── Delete Product ─────────────────────────────────────────────
    @Transactional
    public void deleteProduct(UUID id, UUID vendorId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {
            throw new BadRequestException("You can only delete your own products");
        }

        product.setIsDeleted(true);
        productRepository.save(product);
        log.info("Product deleted: {}", id);
    }

    // ── Private Helpers ────────────────────────────────────────────
    private void logInventory(Product product, int changeQty,
                              String reason, UUID referenceId, int stockAfter) {
        InventoryLog log = InventoryLog.builder()
                .product(product)
                .changeQuantity(changeQty)
                .reason(reason)
                .referenceId(referenceId)
                .stockAfter(stockAfter)
                .build();
        inventoryLogRepository.save(log);
    }

    private void publishViewEvent(UUID productId, UUID customerId) {
        try {
            kafkaTemplate.send(productViewsTopic,
                    productId.toString(),
                    new java.util.HashMap<>() {{
                        put("productId", productId);
                        put("customerId", customerId);
                        put("timestamp", System.currentTimeMillis());
                    }});
        } catch (Exception e) {
            log.warn("Failed to publish view event for product: {}", productId);
        }
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> imageUrls = product.getImages() == null
                ? List.of()
                : product.getImages().stream()
                  .filter(img -> !img.getIsDeleted())
                  .map(ProductImage::getImageUrl)
                  .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .vendorId(product.getVendorId())
                .categoryName(product.getCategory().getName())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .comparePrice(product.getComparePrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .status(product.getStatus())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt())
                .build();
    }
}