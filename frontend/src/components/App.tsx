import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LogIn } from './auth/LogIn';
import Register from './auth/Register';
import Header from './Header';
import { Landing } from './Landing';
import Dashboard from './Dashboard';

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-grow flex items-center justify-center">
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<LogIn />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
