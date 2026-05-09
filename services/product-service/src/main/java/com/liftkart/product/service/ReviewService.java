package com.liftkart.product.service;

import com.liftkart.product.dto.request.CreateReviewRequest;
import com.liftkart.product.dto.response.ReviewResponse;
import com.liftkart.product.entity.Product;
import com.liftkart.product.entity.Review;
import com.liftkart.product.exception.BadRequestException;
import com.liftkart.product.exception.ResourceNotFoundException;
import com.liftkart.product.repository.ProductRepository;
import com.liftkart.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponse createReview(UUID productId,
                                       CreateReviewRequest request,
                                       UUID customerId) {
        if (reviewRepository.existsByProductIdAndCustomerId(productId, customerId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .product(product)
                .customerId(customerId)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(false)
                .build();

        review.setCreatedBy(customerId);
        reviewRepository.save(review);

        // Update product rating cache
        updateProductRating(product);

        return mapToResponse(review);
    }

    public Page<ReviewResponse> getProductReviews(UUID productId, Pageable pageable) {
        return reviewRepository
                .findByProductIdAndIsDeletedFalse(productId, pageable)
                .map(this::mapToResponse);
    }

    private void updateProductRating(Product product) {
        Double avg = reviewRepository.calculateAverageRating(product.getId());
        Long count = reviewRepository.countByProductId(product.getId());

        product.setAverageRating(avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        product.setReviewCount(count != null ? count.intValue() : 0);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomerId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .build();
    }
}