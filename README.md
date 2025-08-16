# Price Drop Tracker

Price Drop Tracker is a microservice-based web application for monitoring product prices and notifying users when prices drop. It supports tracking products from multiple e-commerce platforms and provides a modern frontend dashboard.

## Features

- User registration and authentication
- Track products and set target prices
- Automated price checks and email alerts
- Price history visualization
- Multi-platform scraping (Amazon, Flipkart, etc.)
- Modern frontend UI

## Architecture

- **Frontend**: React + Vite + Tailwind CSS app for user interaction and dashboard. Located in the `frontend/` folder.
- **Backend**: Java 21 application built with [Vert.x](https://vertx.io/). Handles REST API, authentication, product management, scheduling, and email alerts. Located in the `backend/` folder.
- **Scraper Services**:
  - `scraper-v2/`: Python Flask service for scraping Amazon, Flipkart, etc. Modular platform support.
  - `scrapper/`: Legacy Python Flask service for scraping Amazon (uses Selenium).
- **Docker Compose**: Orchestrates all services for local development.

## Prerequisites

- Docker and Docker Compose
- Node.js (for frontend development)
- Java 21 (for backend development)
- Python 3.8+ (for scraper services)
- MongoDB instance
- SendGrid API key

## Configuration

Create an `.env` file inside the `backend` directory with:

```bash
DB_URL=<mongodb-connection-string>
JWT_SECRET=<jwt-secret>
SENDGRID_API_KEY=<sendgrid-api-key>
```

## Running the Application

Build and run all services with Docker Compose:

```bash
docker compose up --build
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Scraper-v2: `http://localhost:8120`
- Scrapper: `http://localhost:8110`

## Frontend Usage

1. Open `http://localhost:5173` in your browser.
2. Register or log in.
3. Add product URLs and set target prices.
4. View tracked products and price history.

## API Endpoints (Backend)

- `POST /api/register` – Register a new user. Requires `userName`, `email`, and `password`.
- `POST /api/login` – Authenticate and receive a JWT token.
- `POST /api/protected/save-product` – Save a product URL with a target price. Requires Authorization header with bearer token.

Example payload:

```json
{
  "productUrl": "https://www.amazon.com/example",
  "targetPrice": "500"
}
```

## Scraper Services

### Scraper-v2

- `POST /scrape` – Expects `{ "urls": ["<product-url>"] }`. Returns price and title for each URL.
- Supports Amazon, Flipkart, and more (see `platforms/` folder).

### Scrapper (Legacy)

- `POST /scrape` – Expects `{ "urls": ["<amazon-url>"] }`. Returns price and title.

## Price Tracking Logic

- Scheduler queries all tracked products every hour.
- Stores price history and sends email alerts when price is within 10% of target price.
- Product is removed from tracking after alert is sent.

## Development

- Frontend: `cd frontend && npm install && npm run dev`
- Backend: `cd backend && ./gradlew run`
- Scraper-v2: `cd scraper-v2 && pip install -r requirements.txt && python app.py`
- Scrapper: `cd scrapper && pip install -r requirements.txt && python app.py`

## Notes

- No production database credentials are included. Supply valid environment variables and run MongoDB separately or use a managed service.
- For production deployment, review and update `fly.toml` and Dockerfiles as needed.

---

Contributions and issues are welcome!
