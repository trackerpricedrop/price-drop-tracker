import React, { useEffect, useState } from "react";
import { useApiFetcher } from "../hooks/useApiFetcher";
import { useAuth } from "../context/AuthContext";
import PriceChart from "./PriceChart";
import { Loader } from "./Loader";
import { useNavigate } from "react-router-dom";
import { ProductService } from "../apis/product/product";

const amazonIcon =
  "https://upload.wikimedia.org/wikipedia/commons/4/4a/Amazon_icon.svg";
const flipkartIcon =
  "https://uxwing.com/wp-content/themes/uxwing/download/brands-and-social-media/flipkart-icon.png";

interface Product {
  productImageUrl: string;
  productTitle: string;
  targetPrice: string;
  productUrl: string;
  productId: string;
}

const formatINR = (price: string | number): string => {
  const amount = typeof price === "string" ? parseFloat(price) : price;
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
};

const Dashboard: React.FC = () => {
  const { authToken, isAuthLoading } = useAuth();
  const navigate = useNavigate();
  const { data, error, loading, fetchData: fetchProducts } = useApiFetcher();
  const { data: deleteResponse, fetchData: fetchDelete } = useApiFetcher();
  const [showChartFor, setShowChartFor] = useState<Product | null>(null);
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);

  useEffect(() => {
    if (isAuthLoading) return;

    if (!authToken) {
      navigate("/login");
      return;
    }

    fetchProducts(
      ProductService.getProduct().url,
      ProductService.getProduct().options
    );
  }, [authToken, isAuthLoading, fetchProducts, navigate]);

  useEffect(() => {
    if (deleteResponse !== null && deleteResponse?.status === 200) {
      const { url, options } = ProductService.getProduct();
      fetchProducts(url, options);
    }
  }, [deleteResponse]);

  const handleDelete = async (productId: string, targetPrice: string) => {
    if (!authToken) return;
    try {
      const deleteRequest = ProductService.deleteProduct(
        productId,
        targetPrice
      );
      fetchDelete(deleteRequest.url, deleteRequest.options);
    } catch (err) {
      console.error("Error deleting product:", err);
    }
  };

  if (isAuthLoading || loading) return <Loader />;
  if (error)
    return <p className="text-center text-red-500">Failed to load products.</p>;
  if (!data || data.status !== 200 || !data.body) return null;

  return (
    <>
      <div className="max-w-7xl mx-auto px-4 pt-4 md:pt-6 pb-10">
        <div className="sticky top-0 z-10 bg-white py-4">
          <h1 className="text-3xl font-bold text-center">
            Your Tracked Products
          </h1>
        </div>

        {data.body.length === 0 ? (
          <p className="text-center text-gray-500 mt-8">
            No products added yet. Start tracking a product to see it here.
          </p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
            {data.body.map((product: Product) => (
              <div
                key={product.productId + product.targetPrice}
                className="bg-white shadow rounded-2xl p-4 flex flex-col h-full relative"
              >
                <div className="absolute top-3 right-3">
                  <button
                    onClick={() =>
                      setOpenMenuId(
                        openMenuId === product.productId + product.targetPrice
                          ? null
                          : product.productId + product.targetPrice
                      )
                    }
                    className="text-xl font-bold text-gray-500 hover:text-gray-800"
                  >
                    ⋮
                  </button>

                  {openMenuId === product.productId + product.targetPrice && (
                    <div className="absolute right-0 mt-2 w-32 bg-white border border-gray-200 shadow-lg rounded-md z-10">
                      <button
                        onClick={() => {
                          handleDelete(product.productId, product.targetPrice);
                          setOpenMenuId(null);
                        }}
                        className="block w-full text-left px-4 py-2 text-sm hover:bg-red-50 text-red-600"
                      >
                        Delete
                      </button>
                    </div>
                  )}
                </div>

                <div className="relative">
                  <img
                    src={
                      product.productId.startsWith("amazon")
                        ? amazonIcon
                        : product.productId.startsWith("flipkart")
                        ? flipkartIcon
                        : undefined
                    }
                    alt="platform"
                    className="w-6 h-6 absolute top-0 left-0 m-2 z-10"
                  />
                  <img
                    src={product.productImageUrl}
                    alt={product.productTitle}
                    className="w-full h-48 object-contain mb-4"
                  />
                </div>
                <a href={product?.productUrl} target="_blank">
                  <h3 className="font-semibold text-lg mb-2">
                    {product.productTitle}
                  </h3>
                </a>

                <div className="mt-auto">
                  <p className="text-red-600 font-semibold">
                    Your set target price: {formatINR(product.targetPrice)}
                  </p>
                  <button
                    onClick={() => setShowChartFor(product)}
                    className="mt-1 text-sm text-blue-600 hover:underline"
                  >
                    View Price History
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showChartFor && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl w-11/12 md:w-2/3 lg:w-1/2 p-6 relative">
            <button
              onClick={() => setShowChartFor(null)}
              className="absolute top-2 right-4 text-gray-600 hover:text-black text-xl font-bold"
            >
              ×
            </button>
            <h2 className="text-xl font-semibold mb-4">
              {showChartFor.productTitle} - Price History
            </h2>
            <PriceChart productId={showChartFor.productId} />
          </div>
        </div>
      )}
    </>
  );
};

export default Dashboard;
