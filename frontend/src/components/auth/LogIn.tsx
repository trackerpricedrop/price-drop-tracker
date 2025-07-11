import React, { useEffect, useState } from 'react';
import { useApiFetcher } from '../../hooks/useApiFetcher';
import { AuthService } from '../../apis/auth/auth';
import { Loader } from '../Loader';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';


export const LogIn = () => {
  const { loading, data, error, fetchData } = useApiFetcher();
  const { login, isAuthenticated } = useAuth();
  const [email, setemail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const GOOGLE_CLIENT_ID = '889837725649-9jmngarfu0q0ufaih8atknhtflu6sir9.apps.googleusercontent.com';

  useEffect(() => {
    if (!isAuthenticated && window.google && GOOGLE_CLIENT_ID) {
      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: handleCredentialResponse,
      });

      window.google.accounts.id.renderButton(
        document.getElementById('google-signin-button'),
        { theme: 'outline', size: 'large' }
      );
    }
  }, [GOOGLE_CLIENT_ID]);

    const handleCredentialResponse = (response: any) => {
    const idToken = response.credential;
    console.log("token", idToken);
    const {url, options} = AuthService.googleLogin(idToken);
    fetchData(url, options);
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    const { url, options } = AuthService.baseLogin(email, password);
    fetchData(url, options);
  };

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (data && data.status === 200 && data.body?.token) {
      login(data.body.token, data.body.user);
      navigate('/');
    }
  }, [data, error, login, navigate]);

  const isFormValid = email.trim() !== '' && password.trim() !== '';

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-blue-50 to-blue-100">
      <div className="bg-white shadow-lg rounded-xl p-8 w-full max-w-md mx-4">
        <h2 className="text-3xl font-extrabold text-gray-900 mb-6 text-center">Log In</h2>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type='email'
              placeholder="email"
              value={email}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              onChange={(event) => setemail(event.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <button
            type="submit"
            disabled={!isFormValid}
            className={`w-full py-3 rounded-lg font-semibold transition ${
              isFormValid
                ? 'bg-blue-600 text-white hover:bg-blue-700'
                : 'bg-gray-400 text-gray-700 cursor-not-allowed'
            }`}
          >
            {loading ? <Loader /> : 'Sign In'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          Don't have an account?{' '}
          <a href="/register" className="text-blue-600 hover:underline">
            Register
          </a>
        </p>
        <p className="m-2 text-center text-sm text-gray-600">
          OR
        </p>
          <div id="google-signin-button" className="m-2"></div>
      </div>
    </div>
  );
};
