import React from "react";
import "../styles/BookCard.css";

export default function BookCard({ book }) {
  return (
    <div className="book-card">
      <img src={book.coverUrl} alt={book.title} />
      <div className="book-info">
        <h3>{book.title}</h3>
        <p>{book.author}</p>
      </div>
    </div>
  );
}
