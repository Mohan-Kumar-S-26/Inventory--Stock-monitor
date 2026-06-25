# StockWatch 🔔
Multi-product stock monitor built with **Spring Boot + PostgreSQL + Jsoup**.  
Tracks products on Flipkart and Croma. Sends email alerts when items come back in stock.  
Exposes a REST API managed via a React frontend or Postman.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Backend      | Java 17, Spring Boot 3.2            |
| Frontend     | React.js                            |
| Database     | PostgreSQL + Spring Data JPA        |
| Scraping     | Jsoup 1.17                          |
| Email        | Spring Mail (Gmail SMTP)            |
| Scheduling   | Spring `@Scheduled`                 |
| Concurrency  | ExecutorService (Fixed Thread Pool) |
| Build        | Maven                               |

---

## How it works

1. Every 5 minutes, `StockCheckerService` fetches all active products from the DB.
2. Products are checked **concurrently** using a fixed thread pool of 10 — each product submitted as an independent task to `ExecutorService`, enabling parallel monitoring.
3. `ScraperService` visits each URL and checks for "Add to Cart" button using Jsoup.
4. If in stock and not yet notified → `EmailService` sends alert and saves to `alert_logs`.
5. The `notified` flag prevents duplicate alerts until item goes out of stock again.

---

## Concurrency Design

Sequential checking meant total wait = N × scrape time per cycle.  
Refactored to submit each product as a `Runnable` to a thread pool:

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
for (Product product : activeProducts) {
    executorService.submit(() -> checkProduct(product));
}
```

Enables parallel scraping across all monitored products simultaneously.

---

## Frontend

Basic React frontend for managing the product watchlist.  
Connects to the Spring Boot REST API.

---

## Setup (Local)

### 1. Prerequisites
- Java 17+
- PostgreSQL running locally
- Gmail account with App Password enabled

### 2. Create the database
```sql
CREATE DATABASE stockwatch;
```

### 3. Set environment variables
DB_USERNAME=postgres

DB_PASSWORD=your_pg_password

GMAIL_USER=yourname@gmail.com

GMAIL_APP_PASSWORD=your_16_char_app_password

### 4. Run
```bash
mvn spring-boot:run
```

---

## API Reference

| Method | Endpoint                      | Description                   |
|--------|-------------------------------|-------------------------------|
| GET    | `/api/products`               | List all watched products      |
| POST   | `/api/products`               | Add a product to watchlist     |
| PATCH  | `/api/products/{id}/pause`    | Pause or resume a product      |
| DELETE | `/api/products/{id}`          | Remove a product               |
| GET    | `/api/products/{id}/alerts`   | Alert history for one product  |
| GET    | `/api/products/alerts/recent` | Last 20 alerts (all products)  |

### Example request body
```json
{
  "name": "OnePlus Nord 4 5G",
  "url": "https://www.flipkart.com/oneplus-nord-4-5g/p/...",
  "email": "yourname@gmail.com"
}
```

---

## Upcoming Features
- Enhanced React dashboard with alert history charts
- Docker + Railway deployment
- Redis caching for scrape results
- Telegram bot notifications
