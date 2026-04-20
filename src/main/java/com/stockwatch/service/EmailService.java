package com.stockwatch.service;

import com.stockwatch.model.AlertLog;
import com.stockwatch.model.Product;
import com.stockwatch.repository.AlertLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AlertLogRepository alertLogRepo;

    public void sendStockAlert(Product product) {
        boolean success = false;
        String errorMessage = null;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(product.getEmail());
            message.setSubject("Stock Alert: " + product.getName() + " is now available!");
            message.setText(buildEmailBody(product));

            mailSender.send(message);
            success = true;
            log.info("Alert sent to {} for product: {}", product.getEmail(), product.getName());

        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("Failed to send alert for {}: {}", product.getName(), e.getMessage());
        }

        // Always log the attempt — success or failure
        AlertLog alertLog = new AlertLog(product, success, errorMessage);
        alertLogRepo.save(alertLog);
    }

    private String buildEmailBody(Product product) {
        return String.format(
            "Good news!\n\n" +
            "%s is back in stock.\n\n" +
            "Buy it here: %s\n\n" +
            "---\n" +
            "StockWatch | You are receiving this because you added this product to your watchlist.",
            product.getName(),
            product.getUrl()
        );
    }
}
