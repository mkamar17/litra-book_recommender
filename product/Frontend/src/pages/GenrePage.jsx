import React, { useState, useEffect } from "react";
import api from "../api/api";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar";
import { motion, AnimatePresence } from "framer-motion";

export default function GenrePage() {
  const [genres] = useState([
    "Fantasy",
    "Romance",
    "Horror",
    "Mystery",
    "Science-fiction",
    "Dystopia"
  ]);
  const [selectedGenre, setSelectedGenre] = useState(null);
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!selectedGenre) return;
    async function fetchBooks() {
      setLoading(true);
      setError(null);
      try {
        const res = await api.get(`/books/genre/${encodeURIComponent(selectedGenre)}`);
        setBooks(res.data);
      } catch (err) {
        console.error(err);
        setError("Could not fetch books.");
      } finally {
        setLoading(false);
      }
    }
    fetchBooks();
  }, [selectedGenre]);

  return (
    <div className="bg-[rgb(24,24,24)] min-h-screen text-white font-poppins">
      <NavBar />


      {/* Genre Selection */}
      <div className="flex flex-wrap gap-3 justify-center mt-8">
        {genres.map((g) => (
          <button
            key={g}
            onClick={() => setSelectedGenre(g)}
            className={`px-4 py-2 rounded-xl transition-all ${
              selectedGenre === g
                ? "bg-blue-600 text-white"
                : "bg-gray-700 hover:bg-gray-600"
            }`}
          >
            {g}
          </button>
        ))}
      </div>

      {/* Content Section */}
      <div className="px-10 mt-8">
        <AnimatePresence>
          {selectedGenre && (
            <motion.div
              key={selectedGenre}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.5 }}
            >
              {loading ? (
                <p className="text-center mt-10 animate-pulse">
                  Fetching {selectedGenre} books...
                </p>
              ) : error ? (
                <p className="text-red-400 text-center mt-10">{error}</p>
              ) : (
                <>
                  <BookRow title={`${selectedGenre}`} books={books} />
                </>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
