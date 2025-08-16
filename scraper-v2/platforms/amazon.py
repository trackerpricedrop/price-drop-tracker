from .base import ECommercePlatform
from playwright.async_api import Page
from urllib.parse import quote_plus
import time
import traceback

class AmazonPlatform(ECommercePlatform):
    async def scrape_product(self, page: Page, url: str) -> dict:
        result = {
            "title": None,
            "price": None,
            "image": None,
            "error": None,
            "status": "success",
            "timings": {}
        }

        try:
            start = time.time()
            await page.goto(url, wait_until="domcontentloaded", timeout=30000)
            result["timings"]["goto"] = round(time.time() - start, 2)

            # Title
            start = time.time()
            title = await page.text_content("#productTitle")
            result["title"] = title.strip() if title else "N/A"
            result["timings"]["title"] = round(time.time() - start, 2)

            # Price
            start = time.time()
            try:
                price_whole = await page.text_content("span.a-price-whole")
                price_fraction = await page.text_content("span.a-price-fraction")

                if price_whole:
                    whole = price_whole.strip().replace(",", "").replace(".", "")
                    fraction = price_fraction.strip() if price_fraction else "00"
                    result["price"] = f"{whole}.{fraction}"
                else:
                    result["price"] = ""
                    result["error"] = "Price not found on Amazon product page"
                    result["status"] = "failure"
            except Exception:
                result["price"] = ""
                result["error"] = "Price not found on Amazon product page"
                result["status"] = "failure"
            result["timings"]["price"] = round(time.time() - start, 2)

            # Image
            start = time.time()
            image = await page.get_attribute("#landingImage", "src")
            result["image"] = image if image else "N/A"
            result["timings"]["image"] = round(time.time() - start, 2)

        except Exception as e:
            result["error"] = str(e)
            result["status"] = "failure"
            traceback.print_exc()

        return result

    async def search(self, page: Page, query: str) -> list:
        results = []
        encoded_query = quote_plus(query)
        url = f"https://www.amazon.in/s?k={encoded_query}"

        try:
            await page.set_extra_http_headers({
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                              "(KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36",
                "Accept-Language": "en-US,en;q=0.9"
            })
            await page.set_viewport_size({"width": 1280, "height": 800})
            await page.goto(url, wait_until="domcontentloaded", timeout=30000)

            await page.wait_for_selector("div.s-main-slot div.s-result-item", timeout=10000)
            items = await page.query_selector_all("div.s-main-slot div.s-result-item")
            for item in items:
                try:
                    # Ensure the item has a data-asin (real product)
                    asin = await item.get_attribute("data-asin")
                    if not asin or asin.strip() == "":
                        continue
                    # Title
                    title_tag = await item.query_selector("h2 span")
                    title = await title_tag.text_content() if title_tag else None
                    if not title:
                        continue

                    # Product URL
                    link_tag = await item.query_selector("a")
                    href = await link_tag.get_attribute("href") if link_tag else None

                    if not href:
                        continue
                    product_url = f"https://www.amazon.in{href.strip().split('?')[0]}"

                    # Image URL (handle lazy-loaded images)
                    img_tag = await item.query_selector("img")
                    image_url = await img_tag.get_attribute("src") or await img_tag.get_attribute("data-src")
                    if not image_url or "data:image" in image_url:
                        image_url = await img_tag.get_attribute("srcset")
                        if image_url and "," in image_url:
                            image_url = image_url.split(",")[0].strip().split(" ")[0]

                    # Price
                    price_tag = await item.query_selector("span.a-price > span.a-offscreen")
                    price_text = await price_tag.text_content() if price_tag else None
                    if not price_text:
                        continue
                    price_value = float(price_text.replace("₹", "").replace(",", "").strip())

                    results.append({
                        "title": title.strip(),
                        "product_url": product_url,
                        "image_url": image_url.strip() if image_url else "N/A",
                        "price": f"{price_value:.2f}",
                        "price_value": price_value
                    })

                    print("[OK]", results[-1])
                    # if len(results) >= 5:
                    #     break

                except Exception as e:
                    print(f"[WARN] Skipping Amazon result due to: {e}")
                    traceback.print_exc()
                    continue

        except Exception as e:
            print(f"[ERROR] Amazon search failed: {e}")
            traceback.print_exc()

        return results

