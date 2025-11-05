import React from 'react';
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AuthPage.css";


export default function AuthPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function handleLogin(e) {
    e.preventDefault();

    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (!res.ok) {
        throw new Error("Invalid credentials");
      }

      const data = await res.json();
      localStorage.setItem("token", data.token);

      navigate("/home"); // redirect home
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="auth-background">
        <h1>Sign In</h1>
      <div className="auth-card">
        
        <form onSubmit={handleLogin}>
          <input
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <input
            placeholder="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button type="submit">Sign In</button>
        </form>

        {error && <p className="error">{error}</p>}

        <p>OR</p>
        <button onClick={() => navigate("/register")}>Get Started →</button>
      </div>
    </div>
  );
}
