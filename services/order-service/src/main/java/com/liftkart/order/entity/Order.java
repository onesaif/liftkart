package com.liftkart.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address_snapshot", nullable = false,
            columnDefinition = "jsonb")
    private Map<String, Object> addressSnapshot;

    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Builder.Default
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "PENDING";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "delivery_charge", precision = 15, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}