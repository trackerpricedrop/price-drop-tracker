from fastapi import FastAPI
from pydantic import BaseModel
from playwright.async_api import async_playwright
import traceback
import time
from contextlib import asynccontextmanager

playwright = None
browser = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global playwright, browser
    print("Starting Playwright and Chromium browser...")
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
    print("✓ Chromium browser ready.")

    yield  # <-- App runs here

    print("Closing browser and Playwright...")
    await browser.close()
    await playwright.stop()
    print("✓ Closed.")

app = FastAPI(lifespan=lifespan)

class ScrapeRequest(BaseModel):
    url: str

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

    page = await browser.new_page()

    # Block heavy resources
    await page.route("**/*", lambda route:
    route.abort() if route.request.resource_type in ["image", "stylesheet", "font", "media"] else route.continue_()
                     )

    try:
        start = time.time()
        await page.goto(url, timeout=30000)
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

    result["timings"]["total"] = round(time.time() - total_start, 2)
    print(f"===== DONE in {result['timings']['total']}s =====\n")
    return result


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
