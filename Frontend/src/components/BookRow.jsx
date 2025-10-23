import React from "react";
import BookCard from "./BookCard";
import "../styles/BookRow.css";

export default function BookRow({ title, books }) {
  if (!books?.length) return null;

  return (
    <div className="mb-8">
      <h2 className="text-xl font-bold text-white mb-3">{title}</h2>
      <div className="book-row">
        {books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
      </div>
    </div>
  );
}
