import { useEffect, useRef, useState } from "react";
import {
  startReadingSession,
  endReadingSession
} from "../api/api";

/**
 * Popup modal component that displays a live reading timer and synchronises reading sessions with the backend.
 */
export default function ReadingTimer({ bookId, title, coverUrl, onClose }) {
  const [seconds, setSeconds] = useState(0);
  const [sessionId, setSessionId] = useState(null);
  const [running, setRunning] = useState(false);

  const intervalRef = useRef(null);

  useEffect(() => {
    let active = true;

    const startSession = async () => {
      const session = await startReadingSession(bookId);
      if (active) {
        setSessionId(session.id);
        setRunning(true);
      }
    };

    startSession();

    return () => {
      active = false;
    };
  }, [bookId]);

  useEffect(() => {
    if (running) {
      intervalRef.current = setInterval(() => {
        setSeconds((s) => s + 1);
      }, 1000);
    }

    return () => clearInterval(intervalRef.current);
  }, [running]);

  useEffect(() => {
    return () => {
      if (sessionId) {
        endReadingSession(sessionId);
      }
    };
  }, [sessionId]);

  const formatTime = (totalSeconds) => {
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;

    return [h, m, s].map(v => String(v).padStart(2, "0")).join(":");
  };

  const handlePause = () => {
    setRunning(false);
  };

  const handleEnd = () => {
    onClose(); // parent unmounts → session ends via cleanup
  };

  return (
    <div className="reading-timer-overlay" onClick={onClose}>
      <div
        className="reading-timer-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <button className="timer-close-btn" onClick={onClose}>
          ✕
        </button>

        <h3>Reading Timer</h3>

        <h1 className="timer-display">{formatTime(seconds)}</h1>

        <div className="timer-book-info">
          <img src={coverUrl} alt={title} />
          <p>{title}</p>
        </div>

        <div className="timer-actions">
          <button className="pause-btn" onClick={handlePause}>
            Pause
          </button>
          <button className="end-btn" onClick={handleEnd}>
            End
          </button>
        </div>
      </div>
    </div>
  );
}
