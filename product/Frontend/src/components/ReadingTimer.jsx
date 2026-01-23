import React, { useEffect, useRef, useState } from "react";
import {
  startReadingSession,
  endReadingSession
} from "../api/api.js";

import {
  Button,
} from "@material-tailwind/react";

/**
 * Reading timer displayed as a card component.
 * Allows starting, pausing, and ending a reading session.
 */
export default function ReadingTimer({ bookId, title, coverUrl, onClose }) {
  const [seconds, setSeconds] = useState(0);
  const [running, setRunning] = useState(false);
  const [sessionId, setSessionId] = useState(null);

  const intervalRef = useRef(null);

  /* -------------------------------
     Timer logic (hh:mm:ss)
  -------------------------------- */
  useEffect(() => {
    if (!running) return;

    intervalRef.current = setInterval(() => {
      setSeconds((s) => s + 1);
    }, 1000);

    return () => clearInterval(intervalRef.current);
  }, [running]);

  /* -------------------------------
     Cleanup on unmount
  -------------------------------- */
  useEffect(() => {
    return () => {
      if (sessionId) {
        endReadingSession(sessionId);
      }
    };
  }, [sessionId]);

  /* -------------------------------
     Helpers
  -------------------------------- */
  const formatTime = (totalSeconds) => {
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;

    return [h, m, s].map(v => String(v).padStart(2, "0")).join(":");
  };

  /* -------------------------------
     Actions
  -------------------------------- */
  const handleStart = async () => {
    if (running || sessionId) return;

    const session = await startReadingSession(bookId);
    setSessionId(session.id);
    setRunning(true);
  };

  const handlePause = () => {
    setRunning(false);
  };

  const handleEnd = async () => {
    setRunning(false);

    if (sessionId) {
      await endReadingSession(sessionId);
    }

    setSessionId(null);
    setSeconds(0);

    if (onClose) onClose();
  };

  /* -------------------------------
     UI - Wrapped in modal overlay
  -------------------------------- */
  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
      onClick={(e) => {
        // Close if clicking the overlay background
        if (e.target === e.currentTarget && !running) {
          handleEnd();
        }
      }}
    >
      <div className="bg-[rgb(24,24,24)] rounded-lg shadow-xl w-96 overflow-hidden">
        {/* Title at the top */}
        <div className="p-6 text-center border-b border-gray-700">
          <h2 className="text-xl font-semibold text-white">
            {title}
          </h2>
        </div>

        {/* Book cover image */}
        <div className="p-6">
          <img
            src={coverUrl}
            alt={title}
            className="h-64 w-full object-cover rounded-lg"
          />
        </div>

        {/* Timer */}
        <div className="text-center py-4">
          <p className="text-5xl font-mono text-white">
            {formatTime(seconds)}
          </p>
        </div>

        {/* Buttons */}
        <div className="flex gap-2 p-6 pt-0">
          {!running ? (
            <Button
              fullWidth
              color="green"
              onClick={handleStart}
              disabled={!!sessionId}
            >
              Start
            </Button>
          ) : (
            <Button
              fullWidth
              color="amber"
              onClick={handlePause}
            >
              Pause
            </Button>
          )}

          <Button
            fullWidth
            color="red"
            onClick={handleEnd}
            disabled={!sessionId}
          >
            End
          </Button>
        </div>
      </div>
    </div>
  );
}