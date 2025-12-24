package com.netconfig.catalog.repository;

import com.netconfig.catalog.domain.Product;
import com.netconfig.catalog.domain.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Product entities.
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    List<Product> findByType(ProductType type);

    List<Product> findByActiveTrue();

    List<Product> findByTypeAndActiveTrue(ProductType type);

    @Query("{ 'attributes.ports': { $gte: ?0 } }")
    List<Product> findSwitchesWithMinPorts(int minPorts);

    @Query("{ 'attributes.capacity_watts': { $gte: ?0 } }")
    List<Product> findPsuWithMinCapacity(int minCapacity);

    @Query("{ 'type': 'RACK', 'attributes.units': { $gte: ?0 } }")
    List<Product> findRacksWithMinUnits(int minUnits);

    boolean existsBySku(String sku);
}

