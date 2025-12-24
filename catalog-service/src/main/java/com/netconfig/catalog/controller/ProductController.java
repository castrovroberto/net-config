package com.netconfig.catalog.controller;

import com.netconfig.catalog.domain.Product;
import com.netconfig.catalog.domain.ProductType;
import com.netconfig.catalog.service.ProductService;
import com.netconfig.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product catalog operations.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false) ProductType type) {
        List<Product> products = (type != null)
                ? productService.getProductsByType(type)
                : productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<Product>> getProductBySku(@PathVariable String sku) {
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    // Specialized queries
    @GetMapping("/switches/min-ports/{minPorts}")
    public ResponseEntity<ApiResponse<List<Product>>> getSwitchesWithMinPorts(
            @PathVariable int minPorts) {
        List<Product> switches = productService.findSwitchesWithMinPorts(minPorts);
        return ResponseEntity.ok(ApiResponse.success(switches));
    }

    @GetMapping("/psu/min-capacity/{minCapacity}")
    public ResponseEntity<ApiResponse<List<Product>>> getPsuWithMinCapacity(
            @PathVariable int minCapacity) {
        List<Product> psus = productService.findPsuWithMinCapacity(minCapacity);
        return ResponseEntity.ok(ApiResponse.success(psus));
    }
}

