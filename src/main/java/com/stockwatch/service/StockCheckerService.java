package com.stockwatch.service;

import com.stockwatch.model.Product;
import com.stockwatch.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCheckerService {

    private final ProductRepository productRepo;
    private final ScraperService scraperService;
    private final EmailService emailService;


    @Scheduled(fixedDelayString = "${stockwatch.check-interval-ms:300000}")
    public void checkAllProducts() {
        List<Product> activeProducts = productRepo.findByActiveTrue();

        if (activeProducts.isEmpty()) {
            log.info("No active products to check.");
            return;
        }

        log.info("Checking {} active products...", activeProducts.size());

        for (Product product : activeProducts) {
            checkProduct(product);
        }

        log.info("Check cycle complete.");
    }

    public void checkSingleProduct(Product product) {
        checkProduct(product);
    }
    private void checkProduct(Product product) {
        log.info("Checking: {}", product.getName());

        boolean inStock = scraperService.isInStock(product.getUrl());
        log.info("Scraper result for {}: inStock={}", product.getName(), inStock);
        product.setLastChecked(LocalDateTime.now());

        if (inStock) {
            product.setLastInStock(LocalDateTime.now());

            if (!product.isNotified()) {

                log.info("IN STOCK: {} — sending alert to {}", product.getName(), product.getEmail());
                emailService.sendStockAlert(product);
                product.setNotified(true);
            } else {
                log.info("Still in stock (alert already sent): {}", product.getName());
            }

        } else {

            if (product.isNotified()) {
                log.info("Back to out-of-stock, resetting alert flag: {}", product.getName());
                product.setNotified(false);
            } else {
                log.info("Still out of stock: {}", product.getName());
            }
        }

        productRepo.save(product);
    }
}
