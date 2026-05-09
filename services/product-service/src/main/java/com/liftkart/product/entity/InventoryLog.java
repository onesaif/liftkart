package com.liftkart.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inventory_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "change_quantity", nullable = false)
    private Integer changeQuantity;     // positive = restock, negative = sold

    @Column(nullable = false)
    private String reason;              // SALE, RESTOCK, ADJUSTMENT

    @Column(name = "reference_id")
    private UUID referenceId;           // order_id if reason = SALE

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;
}