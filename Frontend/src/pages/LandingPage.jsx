import React, { useEffect, useState } from "react";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import '../App.css'

export default function LandingPage() {
  const [books, setBooks] = useState([]);

  useEffect(() => {
    api.get("/books")
      .then((res) => setBooks(res.data))
      .catch(console.error);
  }, []);

  return (
    <div className="bg-grey min-h-screen text-white font-poppins">
    <div className="text-left space-y-10 px-10">
        <BookRow title="For You" books={books} />
    </div>
    </div>

  );
}
