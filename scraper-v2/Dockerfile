FROM python:3.11-slim

# Prevent prompts and reduce image size
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies for Playwright
RUN apt-get update && apt-get install -y \
    wget curl gnupg ca-certificates fonts-liberation \
    libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libgbm1 libasound2 libxshmfence1 libx11-xcb1 libxss1 \
    libxtst6 libgtk-3-0 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the code
COPY app.py .
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Install Playwright and download browser binaries
RUN python -m playwright install --with-deps

# Expose the FastAPI port
EXPOSE 8000

# Run the app using python (since app.py includes __main__ block)
CMD ["python", "app.py"]
