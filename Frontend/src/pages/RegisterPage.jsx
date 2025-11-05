import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AuthPage.css";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function handleRegister(e) {
    e.preventDefault();

    try {
      const res = await fetch("http://localhost:8080/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (!res.ok) {
        throw new Error("Registration failed");
      }

      navigate("/auth");
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="auth-background">
      <div className="auth-wrapper">
        <h2 className="auth-title">Create Account</h2>

        <div className="auth-card">
          <form className="auth-form" onSubmit={handleRegister}>
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

            <button className="auth-button" type="submit">
              Register
            </button>
          </form>

          {error && <p className="error">{error}</p>}

          <p className="divider">Already have an account?</p>

          <button className="secondary-button" onClick={() => navigate("/auth")}>
            Sign In →
          </button>
        </div>
      </div>
    </div>
  );
}
