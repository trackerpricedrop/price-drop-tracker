from .base import ECommercePlatform
from playwright.async_api import Page
from urllib.parse import quote_plus
import time
import traceback

class FlipkartPlatform(ECommercePlatform):
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

            print("[DEBUG] Final URL after redirect:", page.url)

            # Title
            start = time.time()
            title_element = await page.query_selector("h1._6EBuvT")
            title = await title_element.text_content() if title_element else None
            result["title"] = title.strip() if title else "N/A"
            result["timings"]["title"] = round(time.time() - start, 2)

            # Price
            start = time.time()
            price_element = await page.query_selector("div.CxhGGd")
            price_text = await price_element.text_content() if price_element else None
            if price_text:
                price_clean = price_text.replace("₹", "").replace(",", "").strip()
                result["price"] = price_clean
            else:
                result["price"] = ""
                result["error"] = "Price not found on Flipkart product page"
                result["status"] = "failure"
            result["timings"]["price"] = round(time.time() - start, 2)

            # Image
            start = time.time()
            img_element = await page.query_selector("img.jLEJ7H")
            image_url = await img_element.get_attribute("src") if img_element else None
            if not image_url:
                fallback_img = await page.query_selector("img._53J4C-")
                image_url = await fallback_img.get_attribute("src") if fallback_img else None
            result["image"] = image_url or "N/A"
            result["timings"]["image"] = round(time.time() - start, 2)

        except Exception as e:
            result["error"] = str(e)
            result["status"] = "failure"
            traceback.print_exc()

        return result

    async def search(self, page: Page, query: str) -> list:
        results = []
        encoded_query = quote_plus(query)
        url = f"https://www.flipkart.com/search?q={encoded_query}"

        try:
            await page.goto(url, wait_until="domcontentloaded", timeout=30000)
            products = await page.query_selector_all("div.cPHDOP.col-12-12")

            for product in products:
                try:
                    link_tag = await product.query_selector("a.CGtC98")
                    href = await link_tag.get_attribute("href") if link_tag else None
                    if not href or not href.startswith("/"):
                        continue
                    product_url = f"https://www.flipkart.com{href.strip()}"

                    title_tag = await product.query_selector("div.KzDlHZ")
                    title = await title_tag.text_content() if title_tag else None
                    if not title:
                        continue

                    img_tag = await product.query_selector("img.DByuf4")
                    image_url = await img_tag.get_attribute("src") if img_tag else "N/A"

                    price_tag = await product.query_selector("div.Nx9bqj")
                    price_text = await price_tag.text_content() if price_tag else None
                    if not price_text:
                        continue  # ❗ Skip if price not found
                    price_value = float(price_text.replace("₹", "").replace(",", "").strip())

                    results.append({
                        "title": title.strip(),
                        "product_url": product_url,
                        "image_url": image_url.strip() if image_url else "N/A",
                        "price": f"{price_value:.2f}",
                        "price_value": price_value
                    })

                    print("[OK]", results[-1])
                    if len(results) >= 5:
                        break

                except Exception as inner_e:
                    print(f"[WARN] Skipping product due to: {inner_e}")
                    traceback.print_exc()
                    continue

        except Exception as outer_e:
            print(f"[ERROR] Flipkart search failed: {outer_e}")
            traceback.print_exc()

        return results
