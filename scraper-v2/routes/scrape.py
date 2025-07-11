from fastapi import APIRouter, Request
from pydantic import BaseModel
import traceback
import time
from utils import get_browser_context

router = APIRouter()

class ScrapeProductRequest(BaseModel):
    url: str

@router.post("/scrape/product")
async def scrape_product(payload: ScrapeProductRequest, request: Request):
    url = payload.url
    result = {
        "title": None,
        "price": None,
        "error": None,
        "timings": {}
    }

    total_start = time.time()
    print(f"\n===== NEW SCRAPE PRODUCT: {url} =====")

    context = await get_browser_context(request)
    page = await context.new_page()

    await page.set_extra_http_headers({
        "user-agent": "Mozilla/5.0",
        "accept-language": "en-US,en;q=0.9"
    })

    await page.route("**/*", lambda route:
    route.abort() if route.request.resource_type in ["stylesheet", "font", "media"] else route.continue_())

    try:
        start = time.time()
        await page.goto(url, wait_until="domcontentloaded", timeout=30000)
        result["timings"]["goto"] = round(time.time() - start, 2)

        start = time.time()
        title = await page.text_content("#productTitle")
        result["title"] = title.strip() if title else "N/A"
        result["timings"]["title"] = round(time.time() - start, 2)

        start = time.time()
        price_whole = await page.text_content("span.a-price-whole")
        price_fraction = await page.text_content("span.a-price-fraction")
        if price_whole:
            result["price"] = f"{price_whole.strip()}.{price_fraction.strip() if price_fraction else '00'}"
        else:
            result["price"] = "N/A"
        result["timings"]["price"] = round(time.time() - start, 2)

    except Exception as e:
        result["error"] = str(e)
        traceback.print_exc()
    finally:
        await page.close()
        await context.close()

    result["timings"]["total"] = round(time.time() - total_start, 2)
    print(f"===== DONE in {result['timings']['total']}s =====\n")
    return result
