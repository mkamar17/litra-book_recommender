import React, { useState } from "react";
import "../styles/BookCard.css";

export default function BookCard({ book, onAddToLibrary, onStartReading }) {
  const [showPopup, setShowPopup] = useState(false);

  return (
    <div
      className="book-card"
      onMouseEnter={() => setShowPopup(true)}
      onMouseLeave={() => setShowPopup(false)}
    >
      <img src={book.coverUrl} alt={book.title} className="book-cover" />

      {showPopup && (
        <div className="book-popup">
          <div className="popup-header">
            <span className="popup-title">{book.title}</span>
            <button className="popup-add-btn" onClick={() => onAddToLibrary(book)}>+</button>
          </div>

          <p className="popup-author">{book.author}</p>

          <p className="popup-description">
            {book.description?.slice(0, 140) || "No description available."}
          </p>
        </div>
      )}
    </div>
  );
}
