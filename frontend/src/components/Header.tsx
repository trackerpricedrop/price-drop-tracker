import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut } from 'lucide-react';

function Header() {
  const location = useLocation().pathname;
  const isNotLoginOrRegister = location !== '/login';
  const { isAuthenticated, logout, user } = useAuth();
  const showSignIn = !isAuthenticated && isNotLoginOrRegister;

  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement | null>(null);

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <header className="w-full bg-white shadow-md">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
        <Link to="/" className="text-xl font-bold text-blue-600">PriceDrop</Link>

        <nav className="space-x-4 flex items-center">
          {showSignIn && (
            <Link to="/login" className="text-gray-700 hover:text-blue-600">Log In</Link>
          )}
          
          {isAuthenticated && user?.profilePicture && (
            <div className="relative" ref={dropdownRef}>
              <img
                src={user.profilePicture}
                alt="Profile"
                className="w-10 h-10 rounded-full cursor-pointer border-2 border-blue-500"
                onClick={() => setOpen(!open)}
              />
              {open && (
                <div className="absolute right-0 mt-2 w-48 bg-white border rounded-md shadow-lg z-50">
                  <div className="px-4 py-2 text-sm text-gray-700 border-b">{user.name}</div>
                  <button
                    onClick={() => {
                      logout();
                      setOpen(false);
                    }}
                    className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 flex items-center gap-2"
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
