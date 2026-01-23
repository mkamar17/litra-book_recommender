import React, { useEffect, useState } from "react";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/effect-fade";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar.jsx"
import ReadingTimer from "../components/ReadingTimer";
import '../App.css'
import '../styles/BookModal.css'

export default function LandingPage() {
  const [books, setBooks] = useState([]);
  const [thrillerBooks, setThrillerBooks] = useState([]);
  const [fantasyBooks, setFantasyBooks] = useState([]);
  const [romanceBooks, setRomanceBooks] = useState([]);
  const [booktokBooks, setBooktokBooks] = useState([]);
  const [filteredBooks, setFilteredBooks] = useState([]);
  const [loading, setLoading] = useState(true); 
  const [error, setError] = useState(null); 
  const [selectedBook, setSelectedBook] = useState(null);
  const [readingBook, setReadingBook] = useState(null);

  useEffect(() => {
    console.log("selectedBook changed:", selectedBook);
  }, [selectedBook]);

  useEffect(() => {
    console.log("readingBook changed:", readingBook);
  }, [readingBook]);
  
  useEffect(() => {
    async function fetchBooks() {
      try {
        console.log("Fetching books...");
        const [all, thriller, fantasy, romance, booktok] = await Promise.all([
          api.get("/books"),
          api.get("/books/genre/Psychological Thrillers"),
          api.get("/books/genre/Fantasy & YA"),
          api.get("/books/genre/Modern Romance"),
          api.get("/books/genre/BookTok Favourites"),
        ]);
        setBooks(all.data);
        setThrillerBooks(thriller.data);
        setFantasyBooks(fantasy.data);
        setRomanceBooks(romance.data);
        setBooktokBooks(booktok.data);
      } catch (err) {
        console.error("Failed to fetch books:", err);
        setError("Could not load books. Please try again later.");
      } finally {
        setLoading(false); 
      }
    }

    fetchBooks();
  }, []);

  const normalize = (str) =>
    str
      .toLowerCase()
      .replace(/[-''\s]/g, '')
      .trim();

  const handleSearch = (query) => {
    console.log("Search query:", query);
    const normalizedQuery = normalize(query);

    if (normalizedQuery === "") {
      setFilteredBooks([]);
      return;
    }

    const results = books.filter((book) => {
      const title = normalize(book.title || "");
      const genre = normalize(book.genre || "");
      const author = normalize(book.author || "");

      return (
        title.includes(normalizedQuery) ||
        genre.includes(normalizedQuery) ||
        author.includes(normalizedQuery)
      );
    });

    setFilteredBooks(results);
  };

  const handleAddToLibrary = async (book) => {
    try {
      await api.post(`/library/add/${book.id}`);
    } catch (err) {
      console.error("Error adding book:", err);
      alert("Failed to add book");
    }
  };

  const handleSelectBook = (book) => {
    // Don't allow selecting a new book if already in a reading session
    if (readingBook) {
      console.log("Already in reading session");
      return;
    }
    setSelectedBook(book);
  };

  if (loading) {
    return (
      <div className="bg-[rgb(24,24,24)] min-h-screen text-white flex items-center justify-center font-poppins">
        <p className="text-xl animate-pulse">Fetching books...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-[rgb(24,24,24)] min-h-screen text-white flex items-center justify-center font-poppins">
        <p className="text-red-400">{error}</p>
      </div>
    );
  }

  return (
    <div className="bg-[rgb(24,24,24)] min-h-screen text-white font-poppins">
      <NavBar onSearch={handleSearch} />

      <div className="text-left space-y-10 px-10 mt-6">
        {filteredBooks.length > 0 ? (
          <BookRow title="Search Results" books={filteredBooks} onSelectBook={handleSelectBook} />
        ) : (
          <>
            <BookRow title="For You" books={books} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook}/>
            <BookRow title="BookTok Favourites" books={booktokBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook}/>
            <BookRow title="Psychological Thrillers" books={thrillerBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook}/>
            <BookRow title="Fantasy & YA" books={fantasyBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook}/>
            <BookRow title="Modern Romance" books={romanceBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook}/>
          </>
        )}
      </div>

      {/* Book Modal - Keep this separate */}
      {selectedBook && !readingBook && (
        <div
          className="book-modal-overlay"
          onClick={() => setSelectedBook(null)}
        >
          <div className="book-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content-row">
              <div className="modal-left">
                <img
                  src={selectedBook.coverUrl}
                  className="modal-image"
                  alt={selectedBook.title}
                />
                <button
                  className="modal-start-book-btn"
                  onClick={() => {
                    console.log("Starting book:", selectedBook);
                    setReadingBook(selectedBook);
                    setSelectedBook(null);
                  }}
                >
                  Start Book
                </button>
              </div>

              <div className="modal-right">
                <p className="modal-description">
                  {selectedBook.description
                    ? selectedBook.description.replace(/^(.{0,650}\b).*/, "$1") + "…"
                    : "No description available."}
                </p>
              </div>

              <button
                className="modal-close-btn"
                onClick={() => setSelectedBook(null)}
              >
                ✕
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reading Timer - Render at top level */}
      {readingBook && (
        <ReadingTimer
          bookId={readingBook.id}
          title={readingBook.title}
          coverUrl={readingBook.coverUrl}
          onClose={() => {
            console.log("Closing reading session");
            setReadingBook(null);
          }}
        />
      )}
    </div>
  );
}


