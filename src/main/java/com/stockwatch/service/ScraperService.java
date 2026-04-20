package com.stockwatch.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScraperService {


    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    };

    private int agentIndex = 0;


    public boolean isInStock(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(nextUserAgent())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-IN,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .referrer("https://www.google.com")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            String html = doc.html().toLowerCase();
            String pageText = doc.body().text().toLowerCase();


            log.info("Page loaded — length:{} chars", html.length());
            log.info("Page text preview: {}", pageText.substring(0, Math.min(300, pageText.length())));


            boolean outOfStock = pageText.contains("out of stock")
                    || pageText.contains("currently unavailable")
                    || pageText.contains("sold out")
                    || pageText.contains("notify me when available")
                    || pageText.contains("temporarily unavailable");

            log.info("outOfStock signals found: {}", outOfStock);

            return !outOfStock;

        } catch (Exception e) {
            log.error("Scraping failed for URL {}: {}", url, e.getMessage());
            return false;
        }
    }
    private String nextUserAgent() {
        String agent = USER_AGENTS[agentIndex % USER_AGENTS.length];
        agentIndex++;
        return agent;
    }
}
