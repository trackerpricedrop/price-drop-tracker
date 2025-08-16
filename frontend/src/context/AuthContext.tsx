import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import { Product } from "../apis/search/search";
import { User } from "../apis/auth/auth";

interface AuthContextObj {
  authToken: string | null | undefined;
  isAuthLoading: boolean;
  isAuthenticated: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  searchResults: Product[];
  user: User | undefined | null;
  updateSearchState: (results: Product[]) => void;
}

const AuthContext = createContext<AuthContextObj | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [authToken, setAuthToken] = useState<string | null | undefined>(
    undefined
  );
  const [user, setUser] = useState<User | null | undefined>(undefined);
  const [searchResults, setSearchResults] = useState<Product[]>([]);

  useEffect(() => {
    const storedToken = localStorage.getItem("authToken");
    const userString = localStorage.getItem("user");

    if (storedToken) {
      try {
        const payload = JSON.parse(atob(storedToken.split(".")[1]));
        const expiry = payload.exp;
        const now = Math.floor(Date.now() / 1000);

        if (expiry > now) {
          setAuthToken(storedToken);
          setUser(userString ? JSON.parse(userString) : undefined);
        } else {
          setAuthToken(null);
          setUser(undefined);
        }
      } catch (err) {
        console.error("Invalid JWT token:", err);
        setAuthToken(null);
        setUser(undefined);
      }
    } else {
      setAuthToken(null);
      setUser(undefined);
    }
  }, []);

  const login = (token: string, user: User) => {
    localStorage.setItem("authToken", token);
    localStorage.setItem("user", JSON.stringify(user));
    setAuthToken(token);
    setUser(user);
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("user");
    setAuthToken(null);
    setUser(undefined);
  };

  const updateSearchState = (results: Product[]) => {
    setSearchResults(results);
  };

  const isAuthenticated = !!authToken;
  const isAuthLoading = authToken === undefined;

  return (
    <AuthContext.Provider
      value={{
        authToken,
        isAuthLoading,
        isAuthenticated,
        login,
        logout,
        user,
        searchResults,
        updateSearchState,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextObj => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
