import { Loader } from "./Loader";
import React, { useState } from "react";
export const SearchResultCard = ({
  product,
  handleProtectedAction,
  loading,
}: {
  product: {
    image_url: string;
    title: string;
    price: string;
    product_url: string;
  };
  handleProtectedAction: (targetPrice: string, productUrl: string) => void;
  loading: boolean;
}) => {
  const [targetPrice, setTargetPrice] = useState("");
  const handleTrack = (productUrl: string) => {
    if (targetPrice != "") {
      handleProtectedAction(targetPrice, productUrl);
      setTargetPrice("");
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-md p-6 flex flex-col md:flex-row md:items-center gap-6">
      <img
        src={product.image_url}
        alt={product.title}
        className="w-full md:w-32 h-32 object-contain rounded-md flex-shrink-0"
      />

      <div className="flex flex-col flex-grow">
        <a href={product?.product_url} target="_blank">
          <h2 className="text-lg sm:text-xl font-semibold text-gray-900 mb-1">
            {product.title}
          </h2>
        </a>

        <p className="text-blue-600 font-bold mb-4">₹{product.price}</p>
      </div>

      <div
        className="
          flex flex-col sm:flex-row md:flex-col gap-3
          w-full sm:w-auto md:ml-auto
        "
      >
        <input
          type="number"
          placeholder="Target Price"
          value={targetPrice}
          onChange={(e) => setTargetPrice(e.target.value)}
          className="
            border border-gray-300 rounded-lg
            px-4 py-3 text-base
            md:px-3 md:py-2 md:text-sm
            focus:outline-none focus:ring-2 focus:ring-blue-500
            w-full sm:w-auto md:w-32
          "
        />
        <button
          onClick={() => handleTrack(product.product_url)}
          disabled={!targetPrice}
          type="submit"
          className="
            bg-blue-600 text-white font-semibold rounded-lg
            px-5 py-3 text-base
            md:px-4 md:py-2 md:text-sm
            hover:bg-blue-700 transition
          "
        >
          {loading ? <Loader /> : "Add to Track"}
        </button>
      </div>
    </div>
  );
};
