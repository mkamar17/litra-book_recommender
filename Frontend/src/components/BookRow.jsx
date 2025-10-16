import React from "react";
import BookCard from "./BookCard";

export default function BookRow({ title, books }) {
  if (!books?.length) return null;

  return (
    <div className="mb-8">
      <h2 className="text-xl font-bold text-white mb-3">{title}</h2>
      <div className="flex gap-4 overflow-x-auto pb-2">
        {books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
      </div>
    </div>
  );
}
