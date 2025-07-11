const BASE_URL = import.meta.env.VITE_BASE_SCRAPPER_URL;

export interface Product {
    title: string,
    product_url: string,
    image_url: string,
    price: string
}

export const SearchService = {
  search: (searchQuery: string) => {
    return {
      url: `${BASE_URL}/search/amazon?query=${searchQuery}`,
      options: {
        method: 'GET',
      },
    };
  },
};