# Force the container to use the amd64 architecture (important for M1/M2 or arm64 hosts)
FROM --platform=linux/amd64 python:3.11-slim

ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies and Google Chrome (for amd64 only)
RUN apt-get update && apt-get install -y \
    wget gnupg2 ca-certificates curl unzip fonts-liberation \
    libglib2.0-0 libnss3 libgconf-2-4 libxss1 libasound2 \
    libx11-xcb1 libxcomposite1 libxdamage1 libxtst6 libgtk-3-0 \
    libgbm1 && \
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | \
    gpg --dearmor -o /etc/apt/trusted.gpg.d/google.gpg && \
    echo "deb [arch=amd64 signed-by=/etc/apt/trusted.gpg.d/google.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

# Set Chrome binary paths for Selenium/UC
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver

# Create working directory and copy source
WORKDIR /app
COPY . .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000

CMD ["python", "app.py"]
