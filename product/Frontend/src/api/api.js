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
export default api;
