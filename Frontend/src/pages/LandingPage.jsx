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
    <div className="bg-black min-h-screen text-white">
    <h1 className="text-3xl font-bold px-10 pt-6 mb-6">Popular Fiction</h1>
    <div className="space-y-10">
      <BookRow title="For You" books={books} />
    </div>
  </div>

  );
}
