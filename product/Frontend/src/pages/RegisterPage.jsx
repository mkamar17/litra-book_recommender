import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AuthPage.css";

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePassword(password) {
  if (!password) return "Password is required.";
  if (password.length < 8) return "Password must be at least 8 characters.";
  if (password.length > 128) return "Password must be at most 128 characters.";
  if (!/[A-Z]/.test(password)) return "Password must contain at least one capital letter.";
  if (!/[^a-zA-Z0-9]/.test(password)) return "Password must contain at least one special character.";
  return "";
}


export default function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState("");
  const [serverError, setServerError] = useState("");
  const [touched, setTouched] = useState({ email: false, password: false });

  function validate(field, value){
    if (field == "email"){
      if (!value) return "Email is required.";
      if (!validateEmail(value)) return "Please enter a valid email address.";
      return "";
    }
    if (field === "password") return validatePassword(value);
    return "";
  }

  function handleChange(field, value){
    if (field === "email") setEmail(value);
    if (field === "password") setPassword(value);
    if (touched[field]) {
      setErrors((prev) => ({...prev, [field]: validate(field, value)}));
    }
    setServerError("");
  }

  function handleBlur(field) {
    const value = field === "email" ? email : password;
    setTouched((prev) => ({ ...prev, [field]: true }));
    setErrors((prev) => ({ ...prev, [field]: validate(field, value) }));
  }

  async function handleRegister(e) {
    e.preventDefault();

    const emailErr = validate("email", email);
    const passwordErr = validate("password", password);
    setTouched({ email: true, password: true });
    setErrors({ email: emailErr, password: passwordErr });

    try {
      const res = await fetch("http://localhost:8080/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (!res.ok) {
        const body = await res.json().catch(() => null);
  
        const message =
          body?.errors?.[0]?.defaultMessage ||
          body?.message ||
          "Registration failed. Please try again.";
        setServerError(message);
        return;
      }

      navigate("/auth");
    } catch (err) {
      setError("Unable to connect. PLease check your connection.");
    }
  }

  const isFormValid = !errors.email && !errors.password && email && password;

  return (
    <div className="auth-background">
      <div className="auth-wrapper">
        <h2 className="auth-title">Create Account</h2>

        <div className="auth-card">
          <form className="auth-form" onSubmit={handleRegister}>
            <div className="field-group">
              <input
                placeholder="Email"
                type="email"
                value={email}
                onChange={(e) => handleChange("email", e.target.value)}
                onBlur={() => handleBlur("email")}
                className={touched.email && errors.email ? "input-error" : ""}
                aria-invalid={!!(touched.email && errors.email)}
                aria-describedby="email-error"
              />
              {touched.email && errors.email && (
                <p className="field-error" id="email-error">{errors.email}</p>
              )}
            </div>

            <div className="field-group">
              <input
                placeholder="Password"
                type="password"
                value={password}
                onChange={(e) => handleChange("password", e.target.value)}
                onBlur={() => handleBlur("password")}
                className={touched.password && errors.password ? "input-error" : ""}
                aria-invalid={!!(touched.password && errors.password)}
                aria-describedby="password-error"
              />
              {touched.password && errors.password && (
                <p className="field-error" id="password-error">{errors.password}</p>
              )}
            </div>

            <button
              className="auth-button"
              type="submit"
              disabled={touched.email && touched.password && !isFormValid}
            >
              Register
            </button>
          </form>

          {serverError && <p className="error">{serverError}</p>}

          <p className="divider">Already have an account?</p>

          <button className="secondary-button" onClick={() => navigate("/auth")}>
            Sign In →
          </button>
        </div>
      </div>
    </div>
  );
}
