import React, { useEffect } from 'react';
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AuthPage.css";


export default function AuthPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [lockoutSeconds, setLockoutSeconds] = useState(0);

  useEffect(() => {
    if (lockoutSeconds <=0) return;
    const timer = setInterval(() => {
      setLockoutSeconds(prev => {
        if (prev <= 1){
          clearInterval(timer);
          setError("");
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [lockoutSeconds]);

  async function handleLogin(e) {
    e.preventDefault();
    if (lockoutSeconds > 0) return;

    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (res.status == 429) {
        setLockoutSeconds(60);
        setError("Too many login attempts. Please wait 60 seconds.")
        return;
      }
      
      if (!res.ok) {
        setError("Invalid email or password.");
        return;
      }

      const data = await res.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("email", email);

      navigate("/home"); 
    } catch (err) {
      setError("Something went wrong. Please try again.");
    }
  }

  const isLocked = lockoutSeconds > 0;

  return (
    <div className="auth-background">
      <div className="auth-wrapper">
        <h1 className="auth-title">Sign In</h1>
  
        <div className="auth-card">
          <form className="auth-form" onSubmit={handleLogin}>
            <input
              placeholder="Email or phone number"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isLocked}
            />
  
            <input
              placeholder="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={isLocked}
            />
  
            <button type="submit" className="auth-button" disabled={isLocked}>{isLocked ? `Try again in ${lockoutSeconds}s` : "Sign In"}</button>
          </form>
  
          {error && <p className="error">{error}</p>}
  
          <p className="divider">OR</p>
  
          <button onClick={() => navigate("/register")} className="register-button">
            Get Started →
          </button>
        </div>
      </div>
    </div>
  );
  
}
