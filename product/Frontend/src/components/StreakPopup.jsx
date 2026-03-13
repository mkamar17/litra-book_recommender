import React, { useEffect } from "react";
import "../styles/StreakPopup.css";

export default function StreakPopup({ streak, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 4000); // auto-close after 4s
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="streak-popup-overlay" onClick={onClose}>
      <div className="streak-popup" onClick={(e) => e.stopPropagation()}>
        <div className="streak-popup-fire">🔥</div>
        <h2 className="streak-popup-title">{streak} day streak!</h2>
        <p className="streak-popup-sub">
          {streak === 1
            ? "You started a reading streak. Keep it up!"
            : streak < 7
            ? "You're building a great reading habit!"
            : streak < 30
            ? `${streak} days straight — you're on fire! 🔥`
            : `${streak} days — an unstoppable reader!`}
        </p>
        <button className="streak-popup-btn" onClick={onClose}>
          Continue
        </button>
      </div>
    </div>
  );
}