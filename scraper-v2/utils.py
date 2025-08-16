from playwright.async_api import async_playwright
from fastapi import Request

async def get_browser_context(request: Request):
    playwright = request.app.state.playwright
    browser = request.app.state.browser

    try:
        context = await browser.new_context()
    except Exception:
        print("[WARN] Browser dead. Restarting Playwright & Browser...")
        # Restart Playwright and Browser
        playwright = await async_playwright().start()
        browser = await playwright.chromium.launch(headless=True)

        # Update app state
        request.app.state.playwright = playwright
        request.app.state.browser = browser

        context = await browser.new_context()

    return context
