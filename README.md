# StockWatch

Multi-product stock monitor built with **Spring Boot + PostgreSQL + Jsoup**.  
Tracks products on Flipkart and Croma. Sends email alerts when items come back in stock.  
Exposes a REST API so a React frontend (or Postman) can manage the watchlist.

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Backend      | Java 17, Spring Boot 3.2          |
| Database     | PostgreSQL + Spring Data JPA      |
| Scraping     | Jsoup 1.17                        |
| Email        | Spring Mail (Gmail SMTP)          |
| Scheduling   | Spring `@Scheduled`               |
| Build        | Maven                             |

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
Create a `.env` file or set these in your IDE run configuration:
```
DB_USERNAME=postgres
DB_PASSWORD=your_pg_password
GMAIL_USER=yourname@gmail.com
GMAIL_APP_PASSWORD=your_16_char_app_password
```

### 4. Run
```bash
mvn spring-boot:run
```
Spring will auto-create the `products` and `alert_logs` tables on first run.

---

## API Reference

### Products

| Method   | Endpoint                        | Description                  |
|----------|---------------------------------|------------------------------|
| GET      | `/api/products`                 | List all watched products     |
| POST     | `/api/products`                 | Add a product to watchlist    |
| PATCH    | `/api/products/{id}/pause`      | Pause or resume a product     |
| DELETE   | `/api/products/{id}`            | Remove a product              |
| GET      | `/api/products/{id}/alerts`     | Alert history for one product |
| GET      | `/api/products/alerts/recent`   | Last 20 alerts (all products) |

### Add product — example request body
```json
{
  "name": "OnePlus Nord 4 5G",
  "url": "https://www.flipkart.com/oneplus-nord-4-5g/p/...",
  "email": "yourname@gmail.com"
}
```

---

## How it works

1. Every 5 minutes, `StockCheckerService` fetches all active products from the DB.
2. `ScraperService` visits each product URL and checks for "Add to Cart" button presence.
3. If in stock and not yet notified → `EmailService` sends an alert and saves it to `alert_logs`.
4. The `notified` flag on the product prevents duplicate alerts until the item goes out of stock again.

---

## Upcoming features
- React dashboard (add/remove products, view alert history)
- Docker + Railway deployment
- Redis caching for scrape results
- Telegram bot notifications
