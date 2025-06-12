# Price Drop Tracker

Price Drop Tracker is a small microservice project for monitoring product prices and notifying users when prices fall.

## Architecture

- **Backend**: Java 21 application built with [Vert.x](https://vertx.io/).  It provides REST endpoints for user registration, authentication and saving products to track.  Data is stored in MongoDB and email alerts are sent via SendGrid.  A scheduled task checks product prices every hour.
- **Scrapper**: Python Flask service using Selenium and `undetected_chromedriver` to scrape Amazon product pages.  The backend calls this service to fetch the current price and title of a product.
- **Docker Compose**: Orchestrates both containers (`backend` and `scrapper`).

## Prerequisites

- Docker and Docker Compose
- A running MongoDB instance reachable by the backend
- SendGrid API key for sending emails

## Configuration

Create an `.env` file inside the `backend` directory with the following variables:

```bash
DB_URL=<mongodb-connection-string>
JWT_SECRET=<jwt-secret>
SENDGRID_API_KEY=<sendgrid-api-key>
```

These are read at startup by the backend service.

## Running the application

Build and run the services with Docker Compose:

```bash
docker compose up --build
```

The backend listens on `http://localhost:8080` and the scrapper on `http://localhost:8110`.

## API Endpoints

- `POST /api/register` – register a new user. Requires `userName`, `email` and `password` fields.
- `POST /api/login` – authenticate and receive a JWT token.
- `POST /api/protected/save-product` – save a product URL with a target price. Requires an Authorization header with a bearer token. Example payload:

```json
{
  "productUrl": "https://www.amazon.com/example",
  "targetPrice": "500"
}
```

After a product is saved, the scheduler queries all products every hour, stores price history and sends email alerts when the price is within 10% of the specified target price. Once an alert is sent the product is removed from tracking.

## Scrapper Service

The scrapper exposes a single endpoint `POST /scrape` expecting a JSON body:

```json
{ "urls": ["https://www.amazon.com/..."] }
```

It returns price and title information for each URL in the request.

---

This repository contains no production database configuration or credentials.  Ensure you supply valid environment variables and run MongoDB separately or use a managed service.
