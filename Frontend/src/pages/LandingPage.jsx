import React, { useEffect, useState } from "react";
import api from "../api/api.js";
import BookRow from "../components/BookRow";
import NavBar from "../components/NavBar.jsx"
import '../App.css'

export default function LandingPage() {
  const [books, setBooks] = useState([]);

  

  useEffect(() => {
    console.log("LandingPage mounted");
    api.get("/books")
      .then((res) => setBooks(res.data))
      .catch(console.error);
  }, []);

  return (
    <div className="bg-[rgb(24,24,24)] min-h-screen text-white font-poppins">
    <NavBar />
    <div className="text-left space-y-10 px-10 mt-6">
        <BookRow title="For You" books={books} />
    </div>
    </div>

  );
}
