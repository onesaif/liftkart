package com.liftkart.analytics.consumer;

import com.liftkart.analytics.entity.DailySalesSummary;
import com.liftkart.analytics.entity.OrderEventLog;
import com.liftkart.analytics.entity.ProductViewEvent;
import com.liftkart.analytics.repository.DailySalesSummaryRepository;
import com.liftkart.analytics.repository.OrderEventLogRepository;
import com.liftkart.analytics.repository.ProductViewEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {

    private final OrderEventLogRepository orderEventLogRepository;
    private final DailySalesSummaryRepository dailySalesSummaryRepository;
    private final ProductViewEventRepository productViewEventRepository;

    // ── Consume Order Events ───────────────────────────────────────
    @KafkaListener(topics = "${app.kafka.topics.order-events}",
            groupId = "analytics-service")
    @Transactional
    public void handleOrderEvent(Map<String, Object> event) {
        try {
            log.info("Received order event: {}", event);

            UUID orderId = UUID.fromString(event.get("orderId").toString());
            String eventType = event.get("eventType").toString();
            BigDecimal totalAmount = new BigDecimal(
                    event.get("totalAmount").toString());

            // Log every order event permanently
            OrderEventLog eventLog = OrderEventLog.builder()
                    .orderId(orderId)
                    .eventType(eventType)
                    .eventPayload(event)
                    .occurredAt(LocalDateTime.now())
                    .build();
            orderEventLogRepository.save(eventLog);

            // Update daily sales summary
            if ("ORDER_PLACED".equals(eventType)) {
                updateDailySummary(null, totalAmount, 1);
            }

            log.info("Order event processed: {} for order: {}",
                    eventType, orderId);

        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage());
        }
    }

    // ── Consume Product View Events ────────────────────────────────
    @KafkaListener(topics = "${app.kafka.topics.product-views}",
            groupId = "analytics-service")
    @Transactional
    public void handleProductView(Map<String, Object> event) {
        try {
            log.info("Received product view event: {}", event);

            UUID productId = UUID.fromString(
                    event.get("productId").toString());
            UUID customerId = event.get("customerId") != null
                    ? UUID.fromString(event.get("customerId").toString())
                    : null;

            ProductViewEvent viewEvent = ProductViewEvent.builder()
                    .productId(productId)
                    .customerId(customerId)
                    .viewedAt(LocalDateTime.now())
                    .build();

            productViewEventRepository.save(viewEvent);

        } catch (Exception e) {
            log.error("Failed to process product view event: {}",
                    e.getMessage());
        }
    }

    // ── Update Daily Summary ───────────────────────────────────────
    private void updateDailySummary(UUID vendorId,
                                    BigDecimal revenue,
                                    int itemsSold) {
        LocalDate today = LocalDate.now();

        // Update platform-wide summary (vendorId = null)
        DailySalesSummary platformSummary = dailySalesSummaryRepository
                .findByVendorIdIsNullAndSummaryDate(today)
                .orElseGet(() -> DailySalesSummary.builder()
                        .summaryDate(today)
                        .build());

        platformSummary.setTotalOrders(
                platformSummary.getTotalOrders() + 1);
        platformSummary.setTotalRevenue(
                platformSummary.getTotalRevenue().add(revenue));
        platformSummary.setTotalItemsSold(
                platformSummary.getTotalItemsSold() + itemsSold);

        dailySalesSummaryRepository.save(platformSummary);
    }
}