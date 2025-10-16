import React from "react";

export default function BookCard({ book }) {
  return (
    <div className="min-w-[160px] bg-gray-900 rounded-xl overflow-hidden shadow-md hover:scale-105 transition-transform duration-200">
      <img
        src={book.coverUrl}
        alt={book.title}
        className="w-full h-52 object-cover"
      />
      <div className="p-2 text-white">
        <h3 className="text-sm font-semibold truncate">{book.title}</h3>
        <p className="text-xs text-gray-400 truncate">{book.author}</p>
      </div>
    </div>
  );
}
