import React, { useEffect, useState } from "react";
import api from "../api/api.js";
import BookRow from "../components/BookRow";

export default function LandingPage() {
  const [books, setBooks] = useState([]);

//   useEffect(() => {
//     axios
//       .get("http://localhost:8080/api/books")
//       .then((res) => setBooks(res.data))
//       .catch((err) => console.error("Error fetching books:", err));
//   }, []);

  useEffect(() => {
    api.get("/books")
      .then((res) => setBooks(res.data))
      .catch(console.error);
  }, []);

  return (
    <div className="bg-black min-h-screen p-6">
      <h1 className="text-3xl font-bold text-white mb-6">Popular Fiction</h1>
      <BookRow title="Top Picks for You" books={books} />
    </div>
  );
}
