import React, { useEffect, useState } from "react";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/effect-fade";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar.jsx"
import '../App.css'

export default function LandingPage() {
  const [books, setBooks] = useState([]);

  //adding categories to display on homepage

  const [thrillerBooks, setThrillerBooks] = useState([]);
  const [fantasyBooks, setFantasyBooks] = useState([]);
  const [romanceBooks, setRomanceBooks] = useState([]);
  const [booktokBooks, setBooktokBooks] = useState([]);


  const [filteredBooks, setFilteredBooks] = useState([]); //to handle search filtering 
  const [loading, setLoading] = useState(true); 
  const [error, setError] = useState(null); 

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
    .replace(/[-'’\s]/g, '') // normalising hyphens, apostrophes, and spaces
    .trim();

  const handleSearch = (query) => {
    console.log("Search query:", query);

    const normalizedQuery = normalize(query);

    // If the query is empty, reset to main view
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
      alert(`"${book.title}" added to your library!`);
    } catch (err) {
      console.error("Error adding book:", err);
      alert("Failed to add book");
    }
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
        <BookRow title="Search Results" books={filteredBooks} />
      ) : (
        <>
          <BookRow title="For You" books={books} onAddToLibrary={handleAddToLibrary} />
          <BookRow title="BookTok Favourites" books={booktokBooks} onAddToLibrary={handleAddToLibrary} />
          <BookRow title="Psychological Thrillers" books={thrillerBooks} onAddToLibrary={handleAddToLibrary}/>
          <BookRow title="Fantasy & YA" books={fantasyBooks} onAddToLibrary={handleAddToLibrary}/>
          <BookRow title="Modern Romance" books={romanceBooks} onAddToLibrary={handleAddToLibrary}/>
        </>
      )}
    </div>
    </div> );
}
