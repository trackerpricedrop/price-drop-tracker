import React, { useEffect, useState } from "react";
import { SearchResultCard } from "./SearchResultCard";
import { SearchBar } from "./SearchBar";
import { useAuth } from "../context/AuthContext";
import { SearchService } from "../apis/search/search";
import { ProductService } from "../apis/product/addProduct";
import { useApiFetcher } from "../hooks/useApiFetcher";
import { Loader } from "./Loader";
import { PromptModel } from "./PromptModal";
import { useNavigate } from "react-router-dom";

export const Landing = () => {
  const { isAuthenticated, updateSearchState, searchResults } = useAuth();
  const { loading, data, error, fetchData } = useApiFetcher();

  const {
    loading: isProductLoading,
    data: productData,
    fetchData: productFetchData,
  } = useApiFetcher();
  const [clickedProduct, setClickedProduct] = useState("");
  const [modelInfo, setModelInfo] = useState({
    modelType: "",
    isSuccess: false,
    isOpen: false,
  });
  const navigate = useNavigate();
  const handleSearch = (query: string) => {
    if (query !== "") {
      const { url, options } = SearchService.search(query);
      fetchData(url, options);
    } else {
      updateSearchState([]);
    }
  };

  const handleProtectedAction = (targetPrice: string, productUrl: string) => {
    if (!isAuthenticated) {
      setModelInfo({
        modelType: "login",
        isSuccess: true,
        isOpen: true,
      });
    } else {
      setClickedProduct(productUrl);
      const { url, options } = ProductService.addProduct(
        productUrl,
        targetPrice
      );
      productFetchData(url, options).then(() => {
        if (isProductLoading == false && (data !== null || error !== null)) {
          setClickedProduct("");
          if (productData?.status === 200) {
            setModelInfo({
              isSuccess: true,
              isOpen: true,
              modelType: "product-insert",
            });
          } else {
            setModelInfo({
              isSuccess: false,
              isOpen: true,
              modelType: "product-insert",
            });
          }
        }
      });
    }
  };

  const handleLogin = () => {
    setModelInfo({
      modelType: "",
      isSuccess: false,
      isOpen: false,
    });
    navigate("/login");
  };

  useEffect(() => {
    if (data != null && data?.status === 200) {
      console.log("search results from api", data?.body);
      updateSearchState(data?.body);
    }
  }, [data, error]);

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 to-blue-100 px-4 py-12">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center text-gray-900 mb-4">
          Price Drop Tracker
        </h1>
        <p className="text-center text-gray-600 mb-10 max-w-2xl mx-auto">
          Search for your favorite products and track price drops easily.
        </p>
        <PromptModel
          modelInfo={modelInfo}
          onClose={() =>
            setModelInfo((prev) => ({
              ...prev,
              isOpen: false,
            }))
          }
          onLogin={handleLogin}
        />
        <div className="mb-12">
          <SearchBar onSearch={(query) => handleSearch(query)} />
        </div>

        <div className="flex flex-col gap-6">
          {loading ? (
            <Loader />
          ) : error !== null ? (
            "Failed to fetch results"
          ) : (
            searchResults.length > 0 &&
            searchResults.map((result, index) => (
              <SearchResultCard
                key={index}
                product={result}
                handleProtectedAction={handleProtectedAction}
                loading={
                  isProductLoading && result.product_url === clickedProduct
                }
              />
            ))
          )}
        </div>
      </div>
    </div>
  );
};
