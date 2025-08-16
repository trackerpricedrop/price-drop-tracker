from fastapi import FastAPI, Request
from playwright.async_api import async_playwright
from contextlib import asynccontextmanager
import asyncio

from starlette.middleware.cors import CORSMiddleware
from starlette.responses import JSONResponse

from routes.scrape import router as scrape_router
from routes.search import router as search_router


async def start_browser(app: FastAPI):
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
    app.state.playwright = playwright
    app.state.browser = browser
    print("✅ Chromium browser is ready.")


@asynccontextmanager
async def lifespan(app: FastAPI):
    asyncio.create_task(start_browser(app))
    yield
    if hasattr(app.state, "browser"):
        print("🛑 Closing browser & Playwright...")
        await app.state.browser.close()
        await app.state.playwright.stop()
        print("✅ Closed.")

app = FastAPI(lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Include routers
app.include_router(scrape_router)
app.include_router(search_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)