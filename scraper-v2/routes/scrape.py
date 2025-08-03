from fastapi import APIRouter, Request
from pydantic import BaseModel
from utils import get_browser_context
from platforms import get_platform_handler
import traceback
import time
from urllib.parse import urlparse

router = APIRouter()

class ScrapeProductRequest(BaseModel):
    url: str

DOMAIN_PLATFORM_MAP = {
    "amazon.in": "amazon",
    "www.amazon.in": "amazon",
    "amazon.com": "amazon",
    "www.amazon.com": "amazon",
    "flipkart.com": "flipkart",
    "www.flipkart.com": "flipkart",
    "dl.flipkart.com": "flipkart",
    "nykaa.com": "nykaa",
    "www.nykaa.com": "nykaa",
    "myntra.com": "myntra",
    "www.myntra.com": "myntra",
}

def extract_platform_from_url(url: str) -> str:
    domain = urlparse(url).netloc.lower()
    return DOMAIN_PLATFORM_MAP.get(domain)

@router.post("/scrape/product")
async def scrape_product(payload: ScrapeProductRequest, request: Request):
    platform = extract_platform_from_url(payload.url)
    if not platform:
        return {"error": f"Unsupported or unknown domain in URL: {payload.url}"}

    handler = get_platform_handler(platform)
    if not handler:
        return {"error": f"No handler found for platform: {platform}"}

    result = {"error": None, "timings": {}}
    total_start = time.time()

    context = await get_browser_context(request)
    page = await context.new_page()

    await page.set_extra_http_headers({
        "user-agent": "Mozilla/5.0",
        "accept-language": "en-US,en;q=0.9"
    })

    await page.route("**/*", lambda route:
    route.abort() if route.request.resource_type in ["stylesheet", "font", "media"] else route.continue_()
                     )

    try:
        result.update(await handler.scrape_product(page, payload.url))
    except Exception as e:
        result["error"] = str(e)
        traceback.print_exc()
    finally:
        await page.close()
        await context.close()

    result["timings"]["total"] = round(time.time() - total_start, 2)
    return result
