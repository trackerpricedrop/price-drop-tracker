from fastapi import APIRouter, Request, Query
from urllib.parse import quote_plus
import traceback
import time
from utils import get_browser_context

router = APIRouter()

@router.get("/search/amazon")
async def search_amazon(query: str = Query(...), request: Request = None):
    raw_results = []
    start_time = time.time()

    encoded_query = quote_plus(query)
    url = f"https://www.amazon.in/s?k={encoded_query}"
    print(f"\n===> SEARCH: {url}")

    context = await get_browser_context(request)
    page = await context.new_page()

    await page.set_extra_http_headers({
        "user-agent": "Mozilla/5.0",
        "accept-language": "en-US,en;q=0.9"
    })

    try:
        await page.goto(url, wait_until="domcontentloaded")
        products = await page.query_selector_all("div[data-component-type='s-search-result']")

        print(f"Found {len(products)} product blocks")

        for product in products:
            try:
                # ✅ IMAGE & LINK
                image_block = await product.query_selector('div[data-cy="image-container"] a')
                image = "N/A"
                product_link = "N/A"

                if image_block:
                    img = await image_block.query_selector("img.s-image")
                    image = await img.get_attribute("src") if img else "N/A"
                    product_link = await image_block.get_attribute("href") or "N/A"

                # ✅ TITLE — robust
                title = "N/A"
                title_element = await product.query_selector("h2 span")
                if title_element:
                    title = await title_element.text_content()
                else:
                    fallback_title = await product.query_selector("span.a-text-normal")
                    if fallback_title:
                        title = await fallback_title.text_content()

                # ✅ PRICE — numeric
                price_value = None
                price_whole = await product.query_selector("span.a-price-whole")
                price_fraction = await product.query_selector("span.a-price-fraction")
                if price_whole:
                    price_text = f"{await price_whole.text_content()}.{await price_fraction.text_content() if price_fraction else '00'}"
                    try:
                        price_value = float(price_text.replace(",", "").strip())
                    except:
                        price_value = None

                # ✅ Validate link
                if not product_link.strip() or ("/dp/" not in product_link and "/gp/product/" not in product_link):
                    print(f"[SKIP] Not a valid product link: {product_link}")
                    continue

                if not title.strip():
                    continue

                # ✅ Full link
                full_link = f"https://www.amazon.in{product_link.strip()}" if product_link.startswith("/") else product_link

                product_data = {
                    "title": title.strip(),
                    "product_url": full_link,
                    "image_url": image.strip(),
                    "price": f"{price_value:.2f}" if price_value else "N/A",
                    "price_value": price_value  # keep for sorting
                }

                print("[OK]", product_data)
                raw_results.append(product_data)

                if len(raw_results) >= 5:
                    break

            except Exception as e:
                print(f"[WARN] Skipped block: {e}")
                continue

    except Exception as e:
        print(f"[ERROR] {e}")
        traceback.print_exc()
    finally:
        await page.close()
        await context.close()

    print(f"===> DONE: {len(raw_results)} sorted results in {round(time.time() - start_time, 2)}s\n")
    return raw_results
