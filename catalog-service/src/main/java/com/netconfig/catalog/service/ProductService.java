package com.netconfig.catalog.service;

import com.netconfig.catalog.domain.Product;
import com.netconfig.catalog.domain.ProductType;
import com.netconfig.catalog.repository.ProductRepository;
import com.netconfig.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service layer for product operations.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", sku));
    }

    public List<Product> getProductsByType(ProductType type) {
        return productRepository.findByTypeAndActiveTrue(type);
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(product);
        log.info("Created product: {} ({})", saved.getName(), saved.getSku());
        return saved;
    }

    public Product updateProduct(String id, Product updates) {
        Product existing = getProductById(id);
        
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getBasePrice() != null) {
            existing.setBasePrice(updates.getBasePrice());
        }
        if (updates.getAttributes() != null && !updates.getAttributes().isEmpty()) {
            existing.getAttributes().putAll(updates.getAttributes());
        }
        if (updates.getCompatibilityRules() != null && !updates.getCompatibilityRules().isEmpty()) {
            existing.getCompatibilityRules().putAll(updates.getCompatibilityRules());
        }
        
        existing.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(existing);
        log.info("Updated product: {} ({})", saved.getName(), saved.getSku());
        return saved;
    }

    public void deleteProduct(String id) {
        Product product = getProductById(id);
        product.setActive(false);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
        log.info("Soft deleted product: {} ({})", product.getName(), product.getSku());
    }

    public List<Product> findSwitchesWithMinPorts(int minPorts) {
        return productRepository.findSwitchesWithMinPorts(minPorts);
    }

    public List<Product> findPsuWithMinCapacity(int minCapacity) {
        return productRepository.findPsuWithMinCapacity(minCapacity);
    }

    public List<Product> findRacksWithMinUnits(int minUnits) {
        return productRepository.findRacksWithMinUnits(minUnits);
    }
}

