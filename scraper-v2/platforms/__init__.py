from .amazon import AmazonPlatform
from .flipkart import FlipkartPlatform
PLATFORM_HANDLERS = {
    "amazon": AmazonPlatform(),
    "flipkart": FlipkartPlatform(),
}

def get_platform_handler(name: str):
    return PLATFORM_HANDLERS.get(name.lower())
