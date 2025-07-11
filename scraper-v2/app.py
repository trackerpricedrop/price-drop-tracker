from fastapi import FastAPI
from playwright.async_api import async_playwright
from contextlib import asynccontextmanager

from starlette.middleware.cors import CORSMiddleware

from routes.scrape import router as scrape_router
from routes.search import router as search_router

# =============================
# Global Playwright & Browser
# =============================

playwright = None
browser = None

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

# =============================
# FastAPI App
# =============================

app = FastAPI(lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 👈 allow all origins OR set your frontend URL here
    allow_credentials=True,
    allow_methods=["*"],  # or ["GET"] for your use case
    allow_headers=["*"],
)

# Make browser accessible to routers
app.state.playwright = playwright
app.state.browser = browser

# Include routers
app.include_router(scrape_router)
app.include_router(search_router)

# =============================
# Main Entrypoint
# =============================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
