package com.challange.brokeragemanagementapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "asset")
@Getter
@Setter
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String assetName; // Hisse senedi adı veya TRY.

    @Column(nullable = false)
    private BigDecimal size; // Toplam büyüklük.

    @Column(nullable = false)
    private BigDecimal usableSize; // Kullanılabilir büyüklük.

    // Getter, Setter, Constructor methods
}
