import React, { useEffect, useState } from "react";
import {Swiper, SwiperSlide} from "swiper/react";
import "swiper/css";
import { Navigation, EffectFade} from "swiper/modules";
import "swiper/css/navigation";
import "swiper/css/effect-fade";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar.jsx"
import '../App.css'

export default function LandingPage() {
  const [books, setBooks] = useState([]);
  const [filteredBooks, setFilteredBooks] = useState([]); //to handle search filtering 
  const [loading, setLoading] = useState(true); 
  const [error, setError] = useState(null); 

  useEffect(() => {
    async function fetchBooks() {
      try {
        console.log("Fetching books...");
        const res = await api.get("/books");
        setBooks(res.data);
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
      {/* <div className="text-left space-y-10 px-10 mt-6">
        <BookRow title="For You" books={books} />
        <BookRow title="Thrillers" books={books}/>
        <BookRow title="Fantasy" books={books}/>
      </div> */}

<div className="text-left space-y-10 px-10 mt-6">
  {filteredBooks.length > 0 ? (
    // 🔥 Show search results if we have any
    <BookRow title="Search Results" books={filteredBooks} />
  ) : (
    // 🧭 Otherwise show default categories
    <>
      <BookRow title="For You" books={books} />
      <BookRow title="Thrillers" books={books} />
      <BookRow title="Fantasy" books={books} />
    </>
  )}
</div>
    </div>
  );
}
