from fastapi import FastAPI
from pydantic import BaseModel
from playwright.async_api import async_playwright
from contextlib import asynccontextmanager
import traceback
import time

# Global Playwright & Browser
playwright = None
browser = None

# FastAPI lifespan: start/stop Playwright once
@asynccontextmanager
async def lifespan(app: FastAPI):
    global playwright, browser
    print("🚀 Starting Playwright & Chromium...")
    playwright = await async_playwright().start()
    browser = await playwright.chromium.launch(
        headless=True,
        args=[
            "--no-sandbox",
            "--disable-setuid-sandbox",
            "--disable-blink-features=AutomationControlled",
            "--disable-dev-shm-usage",
            "--disable-gpu"
        ]
    )
    print("✅ Chromium browser is ready.")
    yield
    print("🛑 Closing browser & Playwright...")
    await browser.close()
    await playwright.stop()
    print("✅ Closed.")

# Create FastAPI app
app = FastAPI(lifespan=lifespan)

class ScrapeRequest(BaseModel):
    url: str

# Robust helper: get fresh browser context, auto-recover if needed
async def get_browser_context():
    global playwright, browser
    try:
        context = await browser.new_context()
    except Exception:
        print("[WARN] Browser dead. Restarting...")
        if playwright:
            await playwright.stop()
        playwright = await async_playwright().start()
        browser = await playwright.chromium.launch(
            headless=True,
            args=[
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--disable-blink-features=AutomationControlled",
                "--disable-dev-shm-usage",
                "--disable-gpu"
            ]
        )
        context = await browser.new_context()
    return context

@app.post("/scrape")
async def scrape_amazon(payload: ScrapeRequest):
    url = payload.url
    result = {
        "title": None,
        "price": None,
        "error": None,
        "timings": {}
    }

    print(f"\n===== NEW SCRAPE: {url} =====")
    total_start = time.time()

    # Get robust context
    context = await get_browser_context()
    page = await context.new_page()

    # Use realistic headers
    await page.set_extra_http_headers({
        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36",
        "accept-language": "en-US,en;q=0.9"
    })

    # Block heavy resources (safe version: don't block XHR/fetch!)
    await page.route("**/*", lambda route:
    route.abort() if route.request.resource_type in ["image", "stylesheet", "font", "media"] else route.continue_()
                     )

    try:
        start = time.time()
        await page.goto(url, wait_until="domcontentloaded", timeout=30000)
        result["timings"]["goto"] = round(time.time() - start, 2)
        print(f"✓ Page loaded in {result['timings']['goto']}s")

        start = time.time()
        title = await page.text_content("#productTitle")
        result["title"] = title.strip() if title else "not found"
        result["timings"]["title"] = round(time.time() - start, 2)
        print(f"✓ Title: {result['title']} ({result['timings']['title']}s)")

        start = time.time()
        price = await page.text_content("span.a-price-whole")
        result["price"] = price.strip() if price else "not found"
        result["timings"]["price"] = round(time.time() - start, 2)
        print(f"✓ Price: {result['price']} ({result['timings']['price']}s)")

    except Exception as e:
        result["error"] = str(e)
        print("✗ Error:", str(e))
        traceback.print_exc()
    finally:
        await page.close()
        await context.close()  # ✅ Clean up

    result["timings"]["total"] = round(time.time() - total_start, 2)
    print(f"===== DONE in {result['timings']['total']}s =====\n")
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
