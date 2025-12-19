import React, { useEffect, useState } from "react";
import api from "../api/api";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar";

export default function MyLibrary() {
  const [libraryBooks, setLibraryBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchLibrary() {
      try {
        const res = await api.get("/library");
        setLibraryBooks(res.data);
      } catch (err) {
        console.error("Failed to load library:", err);
        setError("Could not load your library.");
      } finally {
        setLoading(false);
      }
    }

    fetchLibrary();
  }, []);

  return (
    <div className="bg-[rgb(24,24,24)] min-h-screen text-white font-poppins">
      <NavBar />

      <div className="px-10 mt-6">
        <h1 className="text-3xl font-bold mb-8">📚 Your Library</h1>

        {loading && <p>Loading your books...</p>}
        {error && <p className="text-red-400">{error}</p>}

        {!loading && libraryBooks.length === 0 && (
          <p>You haven’t added any books yet.</p>
        )}

        {libraryBooks.length > 0 && (
          <BookRow title="Saved Books" books={libraryBooks} />
        )}
      </div>
    </div>
  );
}
