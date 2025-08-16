# Use official Python slim base
FROM python:3.11-slim

# Avoid prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies for Playwright + Chromium
RUN apt-get update && apt-get install -y \
    wget curl gnupg ca-certificates fonts-liberation \
    libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libgbm1 libasound2 libxshmfence1 libx11-xcb1 libxss1 \
    libxtst6 libgtk-3-0 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy only dependency file first (better cache)
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Install Playwright browsers & deps
RUN playwright install-deps && playwright install

# Copy rest of the app code (folders & files)
COPY . .

# Expose FastAPI port
EXPOSE 8000

# Run via uvicorn (recommended for FastAPI)
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
