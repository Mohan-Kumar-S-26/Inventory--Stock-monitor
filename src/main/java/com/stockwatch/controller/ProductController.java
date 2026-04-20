package com.stockwatch.controller;

import com.stockwatch.model.AlertLog;
import com.stockwatch.model.Product;
import com.stockwatch.repository.AlertLogRepository;
import com.stockwatch.repository.ProductRepository;
import com.stockwatch.service.StockCheckerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepo;
    private final AlertLogRepository alertLogRepo;
    @Autowired
    private StockCheckerService stockCheckerService;
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }


    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {


        if (productRepo.existsByUrl(product.getUrl())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "This product URL is already being watched."));
        }
        if (product.getName() == null || product.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product name is required."));
        }
        if (product.getUrl() == null || product.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product URL is required."));
        }
        if (product.getEmail() == null || product.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }

        Product saved = productRepo.save(product);
        CompletableFuture.runAsync(() -> stockCheckerService.checkSingleProduct(saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PostMapping("/{id}/check")
    public ResponseEntity<?> checkNow(@PathVariable Long id) {
        return productRepo.findById(id)
                .map(product -> {
                    // Run check immediately in background
                    CompletableFuture.runAsync(() ->
                            stockCheckerService.checkSingleProduct(product)
                    );
                    return ResponseEntity.ok(
                            Map.of("message", "Check started for " + product.getName())
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<?> togglePause(@PathVariable Long id) {
        return productRepo.findById(id)
                .map(product -> {
                    product.setActive(!product.isActive()); // flip the flag
                    productRepo.save(product);
                    String status = product.isActive() ? "resumed" : "paused";
                    return ResponseEntity.ok(Map.of("message", "Product " + status, "active", product.isActive()));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alertLogRepo.deleteByProductId(id);
        productRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Product removed from watchlist."));
    }


    @GetMapping("/{id}/alerts")
    public ResponseEntity<?> getAlertsForProduct(@PathVariable Long id) {
        if (!productRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<AlertLog> logs = alertLogRepo.findByProductIdOrderBySentAtDesc(id);
        return ResponseEntity.ok(logs);
    }


    @GetMapping("/alerts/recent")
    public List<AlertLog> getRecentAlerts() {
        return alertLogRepo.findTop20ByOrderBySentAtDesc();
    }
}
