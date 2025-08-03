import React, { useEffect, useState } from "react";
import { SearchResultCard } from "./SearchResultCard";
import { SearchBar } from "./SearchBar";
import { useAuth } from "../context/AuthContext";
import { SearchService } from "../apis/search/search";
import { ProductService } from "../apis/product/product";
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
  const [viewMode, setViewMode] = useState<"search" | "paste">("search");
  const [productUrl, setProductUrl] = useState("");
  const [targetPrice, setTargetPrice] = useState("");
  const [selectedPlatform, setSelectedPlatform] = useState<
    "amazon" | "flipkart" | ""
  >("");

  const [modelInfo, setModelInfo] = useState({
    modelType: "",
    isSuccess: false,
    isOpen: false,
    message: "",
  });

  const navigate = useNavigate();

  const handleSearch = (query: string) => {
    if (query !== "" && selectedPlatform !== "") {
      const { url, options } = SearchService.search(query, selectedPlatform);
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
        message: "",
      });
    } else {
      setClickedProduct(productUrl);
      const { url, options } = ProductService.addProduct(
        productUrl,
        targetPrice
      );
      productFetchData(url, options);
    }
  };

  const handlePasteSubmit = () => {
    if (productUrl.trim() !== "" && targetPrice.trim() !== "") {
      handleProtectedAction(targetPrice, productUrl);
    }
  };

  useEffect(() => {
    if (productData != null) {
      setClickedProduct("");
      if (productData?.status === 200) {
        setModelInfo({
          isSuccess: true,
          isOpen: true,
          modelType: "product-insert",
          message:
            "Your Product is added! \n You will be notified when the price drops",
        });
        setProductUrl("");
        setTargetPrice("");
      } else if (productData?.status === 400) {
        setModelInfo({
          isSuccess: true,
          isOpen: true,
          modelType: "product-insert",
          message: "Product with this target price already exists",
        });
      } else {
        setModelInfo({
          isSuccess: false,
          isOpen: true,
          modelType: "product-insert",
          message: "Error inserting the product",
        });
      }
    }
  }, [productData]);

  const handleLogin = () => {
    setModelInfo({
      modelType: "",
      isSuccess: false,
      isOpen: false,
      message: "",
    });
    navigate("/login");
  };

  useEffect(() => {
    if (data != null && data?.status === 200) {
      updateSearchState(data?.body?.results || []);
    }
  }, [data, error]);

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 to-blue-100 px-4 py-12">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center text-gray-900 mb-4">
          Price Drop Tracker
        </h1>
        <p className="text-center text-gray-600 mb-6 max-w-2xl mx-auto">
          Search for your favorite products and track price drops easily.
        </p>

        {/* View Toggle */}
        <div className="flex justify-center gap-4 mb-10">
          <button
            className={`px-4 py-2 rounded-full text-sm font-semibold transition ${
              viewMode === "search"
                ? "bg-blue-600 text-white"
                : "bg-gray-200 text-gray-700"
            }`}
            onClick={() => setViewMode("search")}
          >
            Search Product
          </button>
          <button
            className={`px-4 py-2 rounded-full text-sm font-semibold transition ${
              viewMode === "paste"
                ? "bg-blue-600 text-white"
                : "bg-gray-200 text-gray-700"
            }`}
            onClick={() => setViewMode("paste")}
          >
            Paste Product URL
          </button>
        </div>

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

        {viewMode === "search" ? (
          <>
            {/* Platform Selector */}
            <div className="flex justify-center gap-4 mb-6">
              <button
                className={`flex items-center gap-2 px-4 py-2 rounded-lg border ${
                  selectedPlatform === "amazon"
                    ? "bg-yellow-400 border-yellow-500"
                    : "bg-white border-gray-300"
                }`}
                onClick={() => setSelectedPlatform("amazon")}
              >
                <img
                  src="https://upload.wikimedia.org/wikipedia/commons/4/4a/Amazon_icon.svg"
                  alt="Amazon"
                  className="w-5 h-5"
                />
              </button>
              <button
                className={`flex items-center gap-2 px-4 py-2 rounded-lg border ${
                  selectedPlatform === "flipkart"
                    ? "bg-blue-500 border-blue-600 text-white"
                    : "bg-white border-gray-300"
                }`}
                onClick={() => setSelectedPlatform("flipkart")}
              >
                <img
                  src="https://uxwing.com/wp-content/themes/uxwing/download/brands-and-social-media/flipkart-icon.png"
                  alt="Flipkart"
                  className="w-5 h-5"
                />
              </button>
            </div>

            <div className="mb-12">
              <SearchBar
                onSearch={handleSearch}
                isDisabled={selectedPlatform === ""}
              />
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
          </>
        ) : (
          <div className="max-w-2xl mx-auto bg-white shadow-md rounded-xl p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Paste Product URL
              </label>
              <input
                type="text"
                placeholder="https://www.example.com/product/123"
                value={productUrl}
                onChange={(e) => setProductUrl(e.target.value)}
                className="w-full border rounded-lg p-2"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Set Target Price (₹)
              </label>
              <input
                type="number"
                placeholder="Enter your desired price"
                value={targetPrice}
                onChange={(e) => setTargetPrice(e.target.value)}
                className="w-full border rounded-lg p-2"
              />
            </div>

            <button
              onClick={handlePasteSubmit}
              disabled={!isAuthenticated || isProductLoading}
              className={`w-full py-2 rounded-lg text-white font-semibold ${
                isAuthenticated
                  ? "bg-blue-600 hover:bg-blue-700"
                  : "bg-gray-400 cursor-not-allowed"
              }`}
            >
              {isProductLoading ? "Adding..." : "Add to Track"}
            </button>

            {!isAuthenticated && (
              <p className="text-sm text-red-500 text-center">
                Please log in to track a product.
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
