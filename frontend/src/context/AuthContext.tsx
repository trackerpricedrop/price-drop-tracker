import React, { createContext, useContext, useState, useEffect } from "react";
import { Product } from "../apis/search/search";
import { User } from "../apis/auth/auth";

interface AuthContextObj {
  authToken: null | string;
  login: (token: string, user: User) => void;
  logout: () => void;
  isAuthenticated: boolean;
  searchResults: Product[];
  user: User | undefined | null;
  updateSearchState: (results: Product[]) => void;
}

const AuthContext = createContext<AuthContextObj | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {  
  const [authToken, setAuthToken] = useState<string | null>(null);
  const [searchResults, setSearchResults] = useState<Product[]>([]);
  const [user, setUser] = useState<User>();
  useEffect(() => {
    const storedToken = localStorage.getItem("authToken");
    if (storedToken != "" && storedToken != null) {
      const payload = JSON.parse(atob(storedToken.split(".")[1]));
      const expiry = payload.exp;
      console.log("expiry {}", expiry);
      const now = Math.floor(Date.now() / 1000);
      console.log("now", now);
      if (expiry > now) {
        setAuthToken(storedToken);
      }
    }
  }, []);

  const updateSearchState = (result: Product[]) => {
    setSearchResults(result);
  };

  const login = (token: string, user: User) => {
    localStorage.setItem("authToken", token);
    setAuthToken(token);
    setUser(user);
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    setAuthToken(null);
  };

  const isAuthenticated = !!authToken;

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        login,
        logout,
        user,
        authToken,
        searchResults,
        updateSearchState,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("context not created");
  }
  return context;
};
