import React, { useState } from 'react';
import { Search, X } from 'lucide-react';

export const SearchBar = ({
  onSearch,
}: {
  onSearch: (query: string) => void;
}) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(query);
  };

  const handleClear = () => {
    setQuery('');
    onSearch('');
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex w-full rounded-full overflow-hidden shadow-md bg-white border border-gray-300 focus-within:ring-2 focus-within:ring-blue-500 transition"
    >
      <input
        type="text"
        placeholder="Search for a product..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        className="flex-grow px-5 py-3 text-base focus:outline-none"
      />
      {query && (
        <div style={{display: 'flex', alignItems: 'center'}}>
              <button
  type="button"
  onClick={handleClear}
  className="mx-1 w-6 h-6 rounded-full bg-gray-200 hover:bg-gray-300 text-gray-500 hover:text-gray-700 flex items-center justify-center transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400"
  aria-label="Clear search"
>
  <X className="w-3 h-3 block" />
</button>
        </div>


)}
      <button
        type="submit"
        className="bg-blue-600 hover:bg-blue-700 text-white px-5 flex items-center justify-center transition"
      >
        <Search className="w-5 h-5" />
      </button>
    </form>
  );
};
