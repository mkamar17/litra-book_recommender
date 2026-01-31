import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    console.log("TOKEN:", localStorage.getItem("token"));

    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const startReadingSession = async (bookId) => {
  const res = await api.post(`/reading-sessions/start/${bookId}`);
  return res.data;
};

export const endReadingSession = async (sessionId) => {
  await api.post(`/reading-sessions/end/${sessionId}`);
};

export const setBookProgress = async (bookId, totalPages) => {
  const res = await api.post(`/reading-sessions/progress/${bookId}`, null, {
    params: { totalPages }
  });
  return res.data;
};

export const updatePageReached = async (sessionId, pageReached) => {
  const res = await api.post(`/reading-sessions/end/${sessionId}`, null, {
    params: { pageReached }
  });
  return res.data;
};

export default api;
