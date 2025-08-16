import React, { useEffect, useMemo, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";
import { useApiFetcher } from "../hooks/useApiFetcher";
import { useAuth } from "../context/AuthContext";
import { Loader } from "./Loader";
import dayjs from "dayjs";
import { ProductService } from "../apis/product/product";

interface PriceChartProps {
  productId: string;
}

interface PriceEntry {
  productPrice: string;
  captureTime: string;
}

interface PricePoint {
  date: string;
  price: number;
  rawDate: Date;
}

const formatDate = (date: Date) => dayjs(date).format("DD MMM, HH:mm");

const FILTERS = {
  "1D": 1,
  "1M": 30,
  "3M": 90,
};

const PriceChart: React.FC<PriceChartProps> = ({ productId }) => {
  const { authToken } = useAuth();
  const { data, error, loading, fetchData } = useApiFetcher();
  const [selectedRange, setSelectedRange] =
    useState<keyof typeof FILTERS>("1M");

  useEffect(() => {
    if (authToken && productId) {
      const { url, options } = ProductService.getPriceHistory(productId);
      fetchData(url, options);
    }
  }, [authToken, productId, fetchData]);

  const allChartData: PricePoint[] = useMemo(() => {
    if (!data || data.status !== 200 || !data.body) return [];

    return (data.body as PriceEntry[])
      .map((entry) => {
        const price = parseFloat(entry.productPrice.replace(/[^0-9.]/g, ""));
        const rawDate = new Date(entry.captureTime);
        return {
          rawDate,
          date: formatDate(rawDate), // Updated
          price,
        };
      })
      .filter((entry) => !isNaN(entry.price));
  }, [data]);

  const filteredChartData = useMemo(() => {
    const daysLimit = FILTERS[selectedRange];
    const now = new Date();
    return allChartData.filter(
      (entry) =>
        (now.getTime() - entry.rawDate.getTime()) / (1000 * 60 * 60 * 24) <=
        daysLimit
    );
  }, [allChartData, selectedRange]);

  if (loading) return <Loader />;
  if (error)
    return <p className="text-red-500 text-sm">Failed to load chart.</p>;
  if (allChartData.length === 0)
    return <p className="text-sm text-gray-500">No price history found.</p>;

  return (
    <div className="w-full h-72">
      <div className="flex justify-end mb-2 gap-2">
        {Object.keys(FILTERS).map((key) => (
          <button
            key={key}
            onClick={() => setSelectedRange(key as keyof typeof FILTERS)}
            className={`px-3 py-1 rounded text-sm ${
              selectedRange === key ? "bg-blue-600 text-white" : "bg-gray-200"
            }`}
          >
            {key}
          </button>
        ))}
      </div>

      <ResponsiveContainer width="100%" height="100%">
        <LineChart
          margin={{ top: 20, right: 30, bottom: 40, left: 20 }}
          data={filteredChartData}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 10 }}
            angle={-45}
            textAnchor="end"
            height={60}
          />
          <YAxis
            tickFormatter={(value) =>
              `₹${value.toLocaleString("en-IN", {
                maximumFractionDigits: 0,
              })}`
            }
          />
          <Tooltip formatter={(value) => `₹${value}`} />
          <Line type="monotone" dataKey="price" stroke="#8884d8" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default PriceChart;
