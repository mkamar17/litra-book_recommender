import React, {useEffect, useRef} from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import BookCard from "./BookCard";
import "../styles/BookRow.css";

export default function BookRow({ title, books, onAddToLibrary, onSelectBook}) {
  const swiperRef = useRef(null);
  const observerRef = useRef(null);
  

  const setupObserver = (swiper) => {
    // Disconnect old observer if it exists
    if (observerRef.current) observerRef.current.disconnect();
  
    const swiperEl = swiper.el;
    const slides = swiper.slides;
  
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const slide = entry.target;
          const ratio = entry.intersectionRatio;
  
          // fade if partially visible on either edge
          if (ratio < 0.98) {
            slide.style.opacity = 0.3 + ratio * 0.7; // smoothly fades 0.3 → 1
          } else {
            slide.style.opacity = 1;
          }
        });
      },
      {
        root: swiperEl.querySelector(".swiper-wrapper").parentElement,
        rootMargin: "0px", // detect both edges accurately
        threshold: Array.from({ length: 21 }, (_, i) => i / 20),
      }
    );
  
    slides.forEach((slide) => observer.observe(slide));
    observerRef.current = observer;
  };
  

  useEffect(() => {
    const swiper = swiperRef.current;
    if (!swiper) return;

    // Run once initially
    setupObserver(swiper);

    // Re-run on every navigation / slide change
    swiper.on("slideChangeTransitionEnd", () => setupObserver(swiper));
    swiper.on("resize", () => setupObserver(swiper));

    return () => {
      if (observerRef.current) observerRef.current.disconnect();
      swiper.off("slideChangeTransitionEnd");
      swiper.off("resize");
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
            768: { slidesPerView: 3 },
            480: { slidesPerView: 2 },
          }}
          onSwiper={(swiper) => (swiperRef.current = swiper)}
        >
          {books.map((book) => (
            <SwiperSlide key={book.id || book.externalId}>
              <BookCard book={book} onAddToLibrary={onAddToLibrary} onSelectBook={onSelectBook} />
            </SwiperSlide>
          ))}
        </Swiper>
      </div>
    </div>
  );
}
