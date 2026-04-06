import React, { useEffect, useRef } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import BookCard from "./BookCard";
import "../styles/BookRow.css";

export default function BookRow({ title, books, onAddToLibrary, onSelectBook, refreshProgress, inLibrary }) {
  const swiperRef = useRef(null);
  const observerRef = useRef(null);

  const setupObserver = (swiper) => {
    if (observerRef.current) observerRef.current.disconnect();
    if (!swiper?.el || !swiper?.slides?.length) return;

    const root = swiper.el.querySelector(".swiper-wrapper")?.parentElement;
    if (!root) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const ratio = entry.intersectionRatio;
          entry.target.style.opacity = ratio < 0.98
            ? String(0.3 + ratio * 0.7)
            : "1";
        });
      },
      {
        root,
        rootMargin: "0px",
        threshold: Array.from({ length: 21 }, (_, i) => i / 20),
      }
    );

    swiper.slides.forEach((slide) => observer.observe(slide));
    observerRef.current = observer;
  };

  // Re-run observer whenever books change (new genre loaded)
  useEffect(() => {
    const swiper = swiperRef.current;
    if (!swiper || !books?.length) return;

    // Swiper needs one frame to render the new slides before we observe them
    const id = requestAnimationFrame(() => {
      swiper.update(); // recalculate slide positions
      setupObserver(swiper);
    });

    return () => cancelAnimationFrame(id);
  }, [books]);

  // Wire up Swiper events once on mount
  useEffect(() => {
    const swiper = swiperRef.current;
    if (!swiper) return;

    const onSlideChange = () => setupObserver(swiper);
    const onResize = () => setupObserver(swiper);

    swiper.on("slideChangeTransitionEnd", onSlideChange);
    swiper.on("resize", onResize);

    return () => {
      observerRef.current?.disconnect();
      swiper.off("slideChangeTransitionEnd", onSlideChange);
      swiper.off("resize", onResize);
    };
  }, []);

  if (!books?.length) return null;

  return (
    <div className="mb-8 book-row-container">
      <h2 className="text-xl font-bold text-white mb-3">{title}</h2>
      <div className="book-swiper-mask">
        <Swiper
          modules={[Navigation]}
          navigation
          spaceBetween={0}
          slidesPerView="auto"
          slidesPerGroup={3}
          loop={false}
          className="book-swiper"
          breakpoints={{
            1280: { slidesPerView: 9 },
            1024: { slidesPerView: 5 },
            768:  { slidesPerView: 3 },
            480:  { slidesPerView: 2 },
          }}
          onSwiper={(swiper) => {
            swiperRef.current = swiper;
          }}
        >
          {books.map((book) => (
            <SwiperSlide key={book.id || book.externalId}>
              <BookCard
                book={{ ...book, inLibrary }}
                onAddToLibrary={onAddToLibrary}
                onSelectBook={onSelectBook}
                refreshProgress={refreshProgress}
              />
            </SwiperSlide>
          ))}
        </Swiper>
      </div>
    </div>
  );
}