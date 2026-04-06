import React, { useEffect, useState } from "react";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/effect-fade";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar.jsx"
import ReadingTimer from "../components/ReadingTimer";
import CommentSection from "../components/CommentSection";
import { getAllProgress } from "../api/api.js";
import '../App.css'
import '../styles/BookModal.css'
import StarRating from "../components/StarRating.jsx";

export default function LandingPage() {
  const [books, setBooks] = useState([]);
  const [thrillerBooks, setThrillerBooks] = useState([]);
  const [fantasyBooks, setFantasyBooks] = useState([]);
  const [romanceBooks, setRomanceBooks] = useState([]);
  const [booktokBooks, setBooktokBooks] = useState([]);
  const [recommendedBooks, setRecommendedBooks] = useState([]); 
  const [filteredBooks, setFilteredBooks] = useState([]);
  const [loading, setLoading] = useState(true); 
  const [error, setError] = useState(null); 
  const [selectedBook, setSelectedBook] = useState(null);
  const [readingBook, setReadingBook] = useState(null);
  const [refreshProgress, setRefreshProgress] = useState(0);
  const [continueBooks, setContinueBooks] = useState([]);
  const [selectedBookProgress, setSelectedBookProgress] = useState(0);

  // Add this useEffect — runs when a book is selected
  useEffect(() => {
    if (!selectedBook) return;
    async function fetchSelectedProgress() {
      try {
        const res = await getBookProgress(selectedBook.id);
        if (!res || res.total_pages === 0) return;
        const pct = Math.min(100, (res.current_page / res.total_pages) * 100);
        setSelectedBookProgress(pct);
      } catch (err) {
        setSelectedBookProgress(0);
      }
    }
    fetchSelectedProgress();
  }, [selectedBook]);
  
  useEffect(() => {
    async function fetchBooks() {
      try {
        const [all, thriller, fantasy, romance, booktok, recommended] = await Promise.all([
          api.get("/books"),
          api.get("/books/genre/horror"),
          api.get("/books/genre/fantasy"),
          api.get("/books/genre/romance"),
          api.get("/books/genre/fiction"),
          api.get("/recommendations")
        ]);
        setBooks(all.data);
        setThrillerBooks(thriller.data);
        setFantasyBooks(fantasy.data);
        setRomanceBooks(romance.data);
        setBooktokBooks(booktok.data);
        setRecommendedBooks(recommended.data);
      } catch (err) {
        console.error("Failed to fetch books:", err);
        setError("Could not load books. Please try again later.");
      } finally {
        setLoading(false); 
      }
    }

    fetchBooks();
  }, []);

  useEffect(() => { async function fetchContinueBooks() {
      try {
        const progressList = await getAllProgress();

        const booksInProgress = progressList
          .filter(p => p.currentPage > 0 && p.currentPage < p.totalPages)
          .map(p => books.find(b => b.id === p.book.id))
          .filter(Boolean)

        setContinueBooks(booksInProgress);
      } catch (err) {
        console.error("Failed to fetch continue books: ", err);
      }
  }
    if (books.length > 0) {
      fetchContinueBooks();
    }

  }, [books, refreshProgress]);

  const normalize = (str) =>
    str
      .toLowerCase()
      .replace(/[-''\s]/g, '')
      .trim();

  const handleSearch = (query) => {
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
      alert("Failed to add book");
    }
  };

  const handleSelectBook = (book) => {
    if (readingBook) {
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

  const continueBookIds = new Set(continueBooks.map(b => b.id));

  const forYouBooks = (recommendedBooks.length > 0 ? recommendedBooks : books).filter(b => !continueBookIds.has(b.id)).slice(0,50);

  return (
    <div className="bg-[rgb(24,24,24)] min-h-screen text-white font-poppins">
      <NavBar onSearch={handleSearch} />

      <div className="text-left space-y-10 px-10 mt-6">
        {filteredBooks.length > 0 ? (
          <BookRow title="Search Results" books={filteredBooks} onSelectBook={handleSelectBook} />
        ) : (
          <>
            {continueBooks.length > 0 && (
              <BookRow title="Continue Reading" books={continueBooks} onSelectBook={handleSelectBook} refreshProgress={refreshProgress} inLibrary={true} />
            )}

            <BookRow 
  title="For You" 
  books={forYouBooks} // show recommendations if available, otherwise all books
  onAddToLibrary={handleAddToLibrary} 
  onSelectBook={handleSelectBook} 
  refreshProgress={refreshProgress}
/>
            <BookRow title="Mixed Collection" books={booktokBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook} refreshProgress={refreshProgress}/>
            <BookRow title="Horror" books={thrillerBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook} refreshProgress={refreshProgress}/>
            <BookRow title="Fantasy" books={fantasyBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook} refreshProgress={refreshProgress}/>
            <BookRow title="Romance" books={romanceBooks} onAddToLibrary={handleAddToLibrary} onSelectBook={handleSelectBook} refreshProgress={refreshProgress}/>
          </>
        )}
      </div>

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

                <StarRating bookId={selectedBook.id} />

                <button
                  className="modal-start-book-btn"
                  onClick={() => {
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

                <CommentSection bookId={selectedBook.id} bookProgress={selectedBookProgress} />
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

      {readingBook && (
        <ReadingTimer
          bookId={readingBook.id}
          title={readingBook.title}
          coverUrl={readingBook.coverUrl}
          onClose={() => {
            setReadingBook(null);
            setRefreshProgress(prev => prev + 1); 
          }}
        />
      )}
    </div>
  );
}
