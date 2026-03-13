import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import AuthPage from "./pages/AuthPage";
import RegisterPage from "./pages/RegisterPage";
import GenrePage from "./pages/GenrePage";
import MyLibraryPage from "./pages/MyLibraryPage";
import LeaderboardPage from "./pages/LeaderboardPage";
import ProfilePage from "./pages/ProfilePage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/auth" element={<AuthPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/home" element={<LandingPage />} />
        <Route path="/genre" element={<GenrePage />} />
        <Route path="/library" element={<MyLibraryPage />} />
        <Route path="/" element={<Navigate to="/auth" />} />
        <Route path="/leaderboard" element={<LeaderboardPage/>}/>
        <Route path="/profile" element = {<ProfilePage/>}/>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
