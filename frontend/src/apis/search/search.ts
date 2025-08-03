const BASE_URL = import.meta.env.VITE_BASE_SCRAPPER_URL;

export interface Product {
  title: string;
  product_url: string;
  image_url: string;
  price: string;
}

export const SearchService = {
  search: (query: string, platform: "amazon" | "flipkart") => {
    const encodedQuery = encodeURIComponent(query);
    return {
      url: `${BASE_URL}/search?query=${encodedQuery}&platform=${platform}`,
      options: {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      },
    };
  },
};
