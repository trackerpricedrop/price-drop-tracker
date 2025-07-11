import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import App from './components/App'
import './index.css'
import { AuthProvider } from './context/AuthContext'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/*" element={
          <AuthProvider>
             <App />
          </AuthProvider>
          } />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)
