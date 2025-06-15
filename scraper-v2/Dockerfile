FROM python:3.11-slim

ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies for Chromium
RUN apt-get update && apt-get install -y \
    wget curl gnupg ca-certificates fonts-liberation \
    libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libgbm1 libasound2 libxshmfence1 libx11-xcb1 libxss1 \
    libxtst6 libgtk-3-0 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy files
COPY app.py .
COPY requirements.txt .

# Install Python and Playwright
RUN pip install --no-cache-dir -r requirements.txt

# Correct way to install dependencies and browsers
RUN playwright install-deps && playwright install

EXPOSE 8000

CMD ["python", "app.py"]
