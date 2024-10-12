package com.challange.brokeragemanagementapi.model;

import com.challange.brokeragemanagementapi.model.enumtype.OrderSide;
import com.challange.brokeragemanagementapi.model.enumtype.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String assetName; // Alım veya satım yapılacak varlık.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide orderSide; // BUY veya SELL.

    @Column(nullable = false)
    private BigDecimal size; // Kaç adet hisse alınıyor/satılıyor.

    @Column(nullable = false)
    private BigDecimal price;// Hisse başına fiyat.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PENDING, MATCHED, CANCELED.

    @Column(nullable = false)
    private LocalDateTime createDate; // Siparişin oluşturulma tarihi.

}
