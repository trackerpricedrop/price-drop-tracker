from concurrent.futures import ThreadPoolExecutor, as_completed
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

def get_amazon_details(url):
    options = uc.ChromeOptions()
    options.binary_location = "/usr/bin/google-chrome"
    options.add_argument("--headless=new")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    options.page_load_strategy = 'eager'

    prefs = {
        "profile.managed_default_content_settings.images": 2,
        "profile.default_content_setting_values.notifications": 2,
        "profile.default_content_setting_values.stylesheets": 2,
        "profile.managed_default_content_settings.plugins": 2,
    }
    options.add_experimental_option("prefs", prefs)

    driver = uc.Chrome(options=options, use_subprocess=False)

    result = {
        "title": None,
        "price": None,
        "error": None
    }

    try:
        driver.get(url)
        WebDriverWait(driver, 8).until(EC.presence_of_element_located((By.TAG_NAME, "body")))

        try:
            price_elem = WebDriverWait(driver, 5).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "span.a-price-whole"))
            )
            result["price"] = price_elem.get_attribute("textContent").strip()
        except:
            result["price"] = "not found"

        try:
            title_elem = WebDriverWait(driver, 5).until(
                EC.presence_of_element_located((By.ID, "productTitle"))
            )
            result["title"] = title_elem.text.strip()
        except:
            result["title"] = "not found"

    except Exception as e:
        result["error"] = str(e)
    finally:
        driver.quit()

    return result

def scrape_batch(urls):
    results = []
    with ThreadPoolExecutor(max_workers=4) as executor:
        future_to_url = {executor.submit(get_amazon_details, url): url for url in urls}

        for future in as_completed(future_to_url):
            url = future_to_url[future]
            try:
                data = future.result()
                results.append({"url": url, "data": data})
            except Exception as e:
                results.append({"url": url, "error": str(e)})

    return results
