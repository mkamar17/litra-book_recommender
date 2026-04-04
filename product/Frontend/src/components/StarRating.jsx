import React, { useState, useEffect } from "react";
import { getBookRating, submitBookRating, deleteBookRating } from "../api/api.js";
import "../styles/StarRating.css";

export default function StarRating({ bookId }) {
  const [userRating, setUserRating]   = useState(0);
  const [hovered, setHovered]         = useState(0);
  const [avgRating, setAvgRating]     = useState(0);
  const [ratingCount, setRatingCount] = useState(0);
  const [loading, setLoading]         = useState(true);
  const [saving, setSaving]           = useState(false);

  useEffect(() => {
    if (!bookId) return;
    setLoading(true);
    getBookRating(bookId)
      .then(({ userRating, avgRating, ratingCount }) => {
        setUserRating(userRating);
        setAvgRating(avgRating);
        setRatingCount(ratingCount);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [bookId]);

  async function handleRate(star) {
    if (saving) return;
    setSaving(true);
    try {
      if (star === userRating) {
        await deleteBookRating(bookId);
        setUserRating(0);
        const fresh = await getBookRating(bookId);
        setAvgRating(fresh.avgRating);
        setRatingCount(fresh.ratingCount);
      } else {
        const res = await submitBookRating(bookId, star);
        setUserRating(res.userRating);
        setAvgRating(res.avgRating);
        setRatingCount(res.ratingCount);
      }
    } catch (e) {
      console.error("Rating failed", e);
    } finally {
      setSaving(false);
    }
  }

  const displayed = hovered || userRating;

  if (loading) return <div className="sr-skeleton" />;

  return (
    <div className="sr-full">
      <div className="sr-stars" onMouseLeave={() => setHovered(0)}>
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            className={`sr-star ${star <= displayed ? "sr-star--filled" : ""} ${saving ? "sr-star--disabled" : ""}`}
            onMouseEnter={() => setHovered(star)}
            onClick={() => handleRate(star)}
            aria-label={`Rate ${star} star${star > 1 ? "s" : ""}`}
          >
            ★
          </button>
        ))}
      </div>

      <div className="sr-meta">
        {userRating > 0
          ? <span className="sr-meta__yours">Your rating: {userRating}/5</span>
          : <span className="sr-meta__prompt">Rate this book</span>
        }
        {ratingCount > 0 && (
          <span className="sr-meta__avg">
            ★ {avgRating.toFixed(1)} avg · {ratingCount.toLocaleString()} rating{ratingCount !== 1 ? "s" : ""}
          </span>
        )}
      </div>
    </div>
  );
}