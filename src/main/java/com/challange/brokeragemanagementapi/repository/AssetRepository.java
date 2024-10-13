package com.challange.brokeragemanagementapi.repository;

import com.challange.brokeragemanagementapi.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);

    List<Asset> findAllByCustomerId(Long customerId);

    List<Asset> findByCustomerId(Long customerId);

    Optional<Asset> findByAssetName(String assetName);
}