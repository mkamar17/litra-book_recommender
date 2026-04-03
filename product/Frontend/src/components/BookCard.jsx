import React, { useState, useEffect } from "react";
import "../styles/BookCard.css";
import {
  getBookProgress,
} from "../api/api.js";

export default function BookCard({ book, onAddToLibrary, onSelectBook, refreshProgress}) {
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
      if (res.current_page != 0) console.log("current page:", book.id, res.current_page) 
      if (!res || res.total_pages === 0) return;

      setProgress({
        currentPage: res.current_page,
        totalPages: res.total_pages,
      });
    } catch (err) {
    }
  }

  if (book?.id && book?.inLibrary) {
    fetchProgress();
  }
}, [book.id, refreshProgress]);

  return (
    <div
      className="book-card"
      onMouseEnter={() => setShowPopup(true)}
      onMouseLeave={() => setShowPopup(false)}
      onClick={() => onSelectBook(book)}
    >
      <img src={book.coverUrl} alt={book.title} className="book-cover" referrerPolicy="no-referrer" />

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

      {progress && !showPopup && (
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
