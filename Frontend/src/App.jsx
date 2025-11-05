import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import AuthPage from "./pages/AuthPage";
import RegisterPage from "./pages/RegisterPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/auth" element={<AuthPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/home" element={<LandingPage />} />
        <Route path="/" element={<Navigate to="/auth" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
