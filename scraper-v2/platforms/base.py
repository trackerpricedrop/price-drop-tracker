from abc import ABC, abstractmethod
from playwright.async_api import Page

class ECommercePlatform(ABC):
    @abstractmethod
    async def scrape_product(self, page: Page, url: str) -> dict:
        pass

    @abstractmethod
    async def search(self, page: Page, query: str) -> list:
        pass
