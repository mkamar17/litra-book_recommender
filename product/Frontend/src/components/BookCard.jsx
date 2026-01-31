import React, { useState, useEffect } from "react";
import "../styles/BookCard.css";
import {
  getBookProgress,
} from "../api/api.js";

export default function BookCard({ book, onAddToLibrary, onStartReading, onSelectBook }) {
  const [showPopup, setShowPopup] = useState(false);

  const showAddButton = !!onAddToLibrary;

  const [progress, setProgress] = useState(null);
  const percentageRead =
  progress
    ? Math.min(
        100,
        (progress.currentPage / progress.totalPages) * 100
      )
    : 0;


  useEffect(() => {
    async function fetchProgress() {
      try {
        const res = await getBookProgress(book.id);
        console.log("RAW progress response for book", book.id, res.data.current_page);
        if (!data || data.total_pages === 0) return;
        setProgress({
          currentPage: res.data.current_page,
          totalPages: res.data.total_pages,
        });
      } catch (err) {
        // No progress yet → do nothing
      }
    }
  
    if (book?.id) {
      fetchProgress();
    }
  }, [book?.id]);


  return (
    <div
      className="book-card"
      onMouseEnter={() => setShowPopup(true)}
      onMouseLeave={() => setShowPopup(false)}
      onClick={() => onSelectBook(book)}
    >
      <img src={book.coverUrl} alt={book.title} className="book-cover" />

      {showPopup && (
        <div className="book-popup">
          <div className="popup-header">
            <span className="popup-title">{book.title}</span>

            {showAddButton && (
              <button className="popup-add-btn" onClick={() => onAddToLibrary(book)}>+</button>
            )}
          </div>

          <p className="popup-author">{book.author}</p>

          <p className="popup-description">
            {book.description?.slice(0, 140) || "No description available."}
          </p>
        </div>
      )}

      {progress && (
        <div className="mt-2 w-full h-1 bg-gray-700 rounded">
          <div
            className="h-1 bg-red-600 rounded transition-all duration-300"
            style={{ width: `${percentageRead}%` }}
          />
        </div>
      )}
    </div>
  );
}
