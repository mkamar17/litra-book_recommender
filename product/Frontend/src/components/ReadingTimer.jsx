import React, { useEffect, useRef, useState } from "react";
import StreakPopup from "./StreakPopup"

import {
  startReadingSession,
  setBookProgress,
  updatePageReached,
} from "../api/api.js";

import {
  Button,
  Dialog,
  DialogHeader,
  DialogBody,
  DialogFooter,
  Input,
} from "@material-tailwind/react";

export default function ReadingTimer({ bookId, title, coverUrl, onClose }) {
  const [seconds, setSeconds] = useState(0);
  const [running, setRunning] = useState(false);
  const [sessionId, setSessionId] = useState(null);

  const intervalRef = useRef(null);
  const sessionEndedRef = useRef(false);

  const [showTotalPagesPrompt, setShowTotalPagesPrompt] = useState(false);
  const [totalPages, setTotalPages] = useState("");

  const [showPageReachedPrompt, setShowPageReachedPrompt] = useState(false);
  const [pageReached, setPageReached] = useState("");
  const [currentPage, setCurrentPage] = useState(1);

  const [showSuccessCard, setShowSuccessCard] = useState(false);
  const [sessionResults, setSessionResults] = useState(null);

  const [streakPopup, setStreakPopup] = useState(null);

  useEffect(() => {
    if (!running) return;
    intervalRef.current = setInterval(() => {
      setSeconds((s) => s + 1);
    }, 1000);
    return () => clearInterval(intervalRef.current);
  }, [running]);

  const formatTime = (totalSeconds) => {
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;
    return [h, m, s].map(v => String(v).padStart(2, "0")).join(":");
  };

  const handleStart = async () => {
    if (running || sessionId) return;
    const res = await startReadingSession(bookId);
    
    if (!res.hasProgress) {
      setShowTotalPagesPrompt(true);
      setSessionId(res.sessionId); 
    } else {
      setSessionId(res.sessionId);
      setRunning(true);
      if (res.currentPage) {
        setCurrentPage(res.currentPage);
      }
    }
  };

  const handlePause = () => {
    setRunning(false);
  };

  const handleEnd = () => {
    setRunning(false);
    setShowPageReachedPrompt(true);
  };

  const handleContinueWithPages = async () => {
    await setBookProgress(bookId, totalPages);  
    setShowTotalPagesPrompt(false);
    setRunning(true);
  };

  const handleFinishSession = async () => {
    try {
      if (!pageReached || pageReached === "") {
        alert("Please enter the page you reached");
        return;
      }
    
      const result = await updatePageReached(sessionId, Number(pageReached));
      
      sessionEndedRef.current = true;
      setRunning(false);
      setSessionId(null);
      setSeconds(0);
      setShowPageReachedPrompt(false);

      setSessionResults(result);
      setShowSuccessCard(true);

      if (result.streakDays && result.streakDays > 0){
        setStreakPop(result.streakDays);
      }
      
    } catch (error) {
      console.error("Error finishing session:", error);
    }
  };

  const handleCloseSuccessCard = () => {
    setShowSuccessCard(false);
    setStreakPopup(null);
    window.dispatchEvent(new Event('pointsUpdated'));
    if (onClose) onClose();
  };

  return (
    <>
      <Dialog open={showTotalPagesPrompt} handler={() => setShowTotalPagesPrompt(false)}>
        <DialogHeader>Total pages in this book</DialogHeader>
        <DialogBody>
          <Input
            type="number"
            min="1"
            value={totalPages}
            onChange={(e) => setTotalPages(e.target.value)}
          />
        </DialogBody>
        <DialogFooter>
          <Button onClick={handleContinueWithPages}>
            Continue
          </Button>
        </DialogFooter>
      </Dialog>

      <Dialog open={showPageReachedPrompt} handler={() => setShowPageReachedPrompt(false)}>
        <DialogHeader>What page did you reach?</DialogHeader>
        <DialogBody>
          <Input
            type="number"
            min={currentPage}
            max={totalPages || undefined}
            value={pageReached}
            onChange={(e) => setPageReached(e.target.value)}
          />
        </DialogBody>
        <DialogFooter>
          <Button onClick={handleFinishSession}>
            Finish
          </Button>
        </DialogFooter>
      </Dialog>

       {/* Success Card */}
       <Dialog open={showSuccessCard} handler={handleCloseSuccessCard}>
        <DialogHeader className="flex justify-center">
          <span className="text-2xl">🎉 Well Done! 🎉</span>
        </DialogHeader>
        <DialogBody className="text-center">
          {sessionResults && (
            <div className="space-y-4">
              <p className="text-lg">
                You have read <span className="font-bold text-green-600">{sessionResults.pagesRead}</span> pages
              </p>
              <p className="text-lg">
                and earned <span className="font-bold text-blue-600">{sessionResults.pointsAwarded}</span> points!
              </p>
              <p className="text-sm text-gray-600">
                Reading time: {formatTime(sessionResults.durationSeconds)}
              </p>
            </div>
          )}
        </DialogBody>
        <DialogFooter className="flex justify-center">
          <Button color="green" onClick={handleCloseSuccessCard}>
            Awesome!
          </Button>
        </DialogFooter>
      </Dialog>

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
          if (e.target === e.currentTarget && !running) {
            if (onClose) onClose();
          }
        }}
      >
        <div className="bg-[rgb(24,24,24)] rounded-lg shadow-xl w-96 overflow-hidden">
          <div className="p-6 text-center border-b border-gray-700">
            <h2 className="text-xl font-semibold text-white">{title}</h2>
          </div>

          <div className="p-6">
            <img
              src={coverUrl}
              alt={title}
              className="h-64 w-full object-cover rounded-lg"
            />
          </div>

          <div className="text-center py-4">
            <p className="text-5xl font-mono text-white">
              {formatTime(seconds)}
            </p>
          </div>

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

      {streakPopup && (
        <StreakPopup
          streak = {streakPopup}
          onClose={() => setStreakPopup(null)}
        />
      )}
    </>
  );
}