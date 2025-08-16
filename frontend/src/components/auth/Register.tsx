import React, { use, useEffect, useState } from 'react'
import { ReactFormState } from 'react-dom/client'
import { User, AuthService} from '../../apis/auth/auth';
import { useApiFetcher } from '../../hooks/useApiFetcher';
import { useNavigate } from 'react-router-dom';
function Register() {

  const {loading, data, error, fetchData} = useApiFetcher();
  const navigate = useNavigate();
  const [user, setUser] = useState<User>({
    name: '',
    email: '',
    password: '',
    profilePicture: '',
  });
  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    const {url, options} = AuthService.baseRegister(user);
    fetchData(url, options);
  }
  useEffect(() => {
    if(data && data?.status === 200) {
      navigate('/');
    }
  }, [data, error])

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-green-50 to-green-100">
      <div className="bg-white shadow-lg rounded-xl p-8 w-full max-w-md mx-4">
        <h2 className="text-3xl font-extrabold text-gray-900 mb-6 text-center">Create an Account</h2>
        <form className="space-y-5" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
            <input
              value={user.name}
              type="text"
              placeholder="Rahul"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
              onChange={(event) => setUser((prev) => {
                return {
                  ...prev, 
                  name: event.target.value
                }
              } )}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              value={user.email}
              type="email"
              placeholder="you@example.com"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
              onChange={(event) => setUser((prev) => {
                return {
                  ...prev, 
                  email: event.target.value
                }
              })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              value={user.password}
              type="password"
              placeholder="Create a password"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
              onChange={(event) => setUser((prev) => {
                return {
                  ...prev, 
                  password: event.target.value
                }
              })}
            />
          </div>
          <button
            type="submit"
            className="w-full bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 transition"
          >
            Register
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-gray-600">
          Already have an account?{' '}
          <a href="/login" className="text-green-600 hover:underline">
            Log In
          </a>
        </p>
      </div>
    </div>
  )
}

export default Register
