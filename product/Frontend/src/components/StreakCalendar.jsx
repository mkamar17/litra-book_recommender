import React, { useState, useEffect, useRef } from "react";
import "../styles/StreakCalendar.css";

const DAYS = ["M", "T", "W", "T", "F", "S", "S"];
const MONTHS = [
  "January","February","March","April","May","June",
  "July","August","September","October","November","December"
];

async function fetchStreak() {
  const token = localStorage.getItem("token");
  const res = await fetch("http://localhost:8080/api/users/streak", {
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.json();
}

export default function StreakCalendar() {
  const [open, setOpen] = useState(false);
  const [streakData, setStreakData] = useState(null);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const ref = useRef(null);

  useEffect(() => {
    // fetch streak count for navbar badge on mount
    fetchStreak().then(setStreakData).catch(() => {});
  }, []);

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  async function handleOpen() {
    if (open) { setOpen(false); return; }
    try {
      const data = await fetchStreak();
      setStreakData(data);
    } catch (e) {}
    setOpen(true);
  }

  function getDaysInMonth(date) {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  }

  function getFirstDayOfMonth(date) {
    // Monday-based: 0=Mon, 6=Sun
    const day = new Date(date.getFullYear(), date.getMonth(), 1).getDay();
    return (day + 6) % 7;
  }

  function isReadDate(day) {
    if (!streakData?.readDates) return false;
    const d = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    const str = d.toISOString().split("T")[0];
    return streakData.readDates.includes(str);
  }

  function isToday(day) {
    const today = new Date();
    return (
      day === today.getDate() &&
      currentMonth.getMonth() === today.getMonth() &&
      currentMonth.getFullYear() === today.getFullYear()
    );
  }

  function prevMonth() {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
  }

  function nextMonth() {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
  }

  const daysInMonth = getDaysInMonth(currentMonth);
  const firstDay = getFirstDayOfMonth(currentMonth);
  const cells = Array(firstDay).fill(null).concat(
    Array.from({ length: daysInMonth }, (_, i) => i + 1)
  );
  // pad to complete last row
  while (cells.length % 7 !== 0) cells.push(null);

  const currentStreak = streakData?.currentStreak ?? 0;
  const longestStreak = streakData?.longestStreak ?? 0;

  return (
    <div className="streak-wrapper" ref={ref}>
      {/* Fire button */}
      <button className="streak-btn" onClick={handleOpen}>
        <span className="streak-fire">🔥</span>
        <span className="streak-count">{currentStreak}</span>
      </button>

      {/* Dropdown */}
      {open && (
        <div className="streak-dropdown">
          {/* Stats row */}
          <div className="streak-stats">
            <div className="streak-stat">
              <span className="streak-stat-icon">🔥</span>
              <div>
                <p className="streak-stat-value">{currentStreak} days</p>
                <p className="streak-stat-label">Current streak</p>
              </div>
            </div>
            <div className="streak-stat-divider" />
            <div className="streak-stat">
              <span className="streak-stat-icon">🏆</span>
              <div>
                <p className="streak-stat-value">{longestStreak} days</p>
                <p className="streak-stat-label">Longest streak</p>
              </div>
            </div>
          </div>

          {/* Calendar header */}
          <div className="streak-cal-header">
            <button className="streak-nav-btn" onClick={prevMonth}>‹</button>
            <span className="streak-month-label">
              {MONTHS[currentMonth.getMonth()].toUpperCase()} {currentMonth.getFullYear()}
            </span>
            <button className="streak-nav-btn" onClick={nextMonth}>›</button>
          </div>

          {/* Day labels */}
          <div className="streak-day-labels">
            {DAYS.map((d, i) => (
              <span key={i} className="streak-day-label">{d}</span>
            ))}
          </div>

          {/* Calendar grid */}
          <div className="streak-grid">
            {cells.map((day, i) => (
              <div
                key={i}
                className={`streak-cell ${
                  day && isReadDate(day) ? "streak-cell--read" : ""
                } ${day && isToday(day) ? "streak-cell--today" : ""} ${
                  !day ? "streak-cell--empty" : ""
                }`}
              >
                {day || ""}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}