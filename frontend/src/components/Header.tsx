import React, { useState, useRef, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { LogOut } from "lucide-react";

function Header() {
  const location = useLocation().pathname;
  const isNotLoginOrRegister =
    location !== "/login" && location !== "/register";
  const { isAuthenticated, logout, user } = useAuth();
  const showSignIn = !isAuthenticated && isNotLoginOrRegister;

  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <header className="w-full bg-white shadow-md border-b">
      <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
        <Link
          to="/"
          className="text-2xl font-bold text-blue-600 tracking-tight"
        >
          PriceDrop
        </Link>

        <nav className="flex items-center space-x-6">
          {isAuthenticated && (
            <Link
              to="/dashboard"
              className="text-gray-700 hover:text-blue-600 text-sm font-medium transition-colors"
            >
              Dashboard
            </Link>
          )}

          {showSignIn && (
            <Link
              to="/login"
              className="text-gray-700 hover:text-blue-600 text-sm font-medium transition-colors"
            >
              Log In
            </Link>
          )}

          {isAuthenticated && user?.profilePicture && (
            <div className="relative" ref={dropdownRef}>
              <img
                src={user.profilePicture}
                alt="Profile"
                className="w-10 h-10 rounded-full cursor-pointer border-2 border-blue-500 hover:opacity-90 transition"
                onClick={() => setOpen(!open)}
              />
              {open && (
                <div className="absolute right-0 mt-3 w-52 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                  <div className="px-4 py-3 text-sm text-gray-800 font-semibold border-b bg-gray-50">
                    {user.name}
                  </div>
                  <button
                    onClick={() => {
                      logout();
                      setOpen(false);
                    }}
                    className="w-full text-left px-4 py-3 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2 transition-colors"
                  >
                    <LogOut className="w-4 h-4" />
                    Log Out
                  </button>
                </div>
              )}
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Header;
