import React, {useState} from 'react'
import { Routes, Route } from 'react-router-dom'
import { Product } from '../apis/search/search'
import {LogIn} from './auth/LogIn'
import Register from './auth/Register'
import Header from './Header'
import { Landing } from './Landing'

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-grow flex items-center justify-center">
        <Routes>
          <Route path="/" element={<Landing/>} />
          <Route path="/login" element={<LogIn />} />
          <Route path="/register" element={<Register />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
