from fastapi import FastAPI
from pydantic import BaseModel
from playwright.async_api import async_playwright

app = FastAPI()

# Define the request payload schema
class ScrapeRequest(BaseModel):
    url: str

@app.post("/scrape")
async def scrape_amazon(payload: ScrapeRequest):
    url = payload.url

    result = {
        "title": None,
        "price": None,
        "error": None
    }

    try:
        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=True)
            page = await browser.new_page()
            await page.goto(url, timeout=15000)

            try:
                title = await page.text_content("#productTitle")
                result["title"] = title.strip() if title else "not found"
            except:
                result["title"] = ""

            try:
                price = await page.text_content("span.a-price-whole")
                result["price"] = price.strip() if price else "not found"
            except:
                result["price"] = ""

            await browser.close()
    except Exception as e:
        result["error"] = str(e)

    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
