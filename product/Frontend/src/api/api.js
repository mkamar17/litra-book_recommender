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

export const getBookProgress = async (bookId) => {
  const res = await api.get(`/reading-sessions/progress/${bookId}`);
  return res.data;
};

export const getAllProgress = async () => {
  const res = await api.get('/reading-sessions/progress');
  return res.data;
};

export const updatePageReached = async (sessionId, pageReached) => {
  const response = await fetch(
    `http://localhost:8080/api/reading-sessions/end/${sessionId}?pageReached=${pageReached}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  );

  if (!response.ok) {
    throw new Error('Failed to end reading session');
  }

  return await response.json(); 
};

export const getUserTotalPoints = async () => {
  const response = await fetch('http://localhost:8080/api/users/total-points', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  });

  if (!response.ok) {
    throw new Error('Failed to fetch total points');
  }

  return await response.json();
};

export const getRecommendations = async () => {
  const res = await api.get(`/recommendations`);
  return res.data;
};

export const getComments = async (bookId) => {
  const res = await api.get(`/books/${bookId}/comments`);
  return res.data;
};

export const postComment = async (bookId, content, parentCommentId = null) => {
  const body = parentCommentId ? {content, parentCommentId} : {content};
  const res = await api.post(`/books/${bookId}/comments`, body);
  return res.data;
};

export const deleteComment = async (bookId, commentId) => {
  await api.delete(`/books/${bookId}/comments/${commentId}`);
};

export const getNotifications = async () => {
  const res = await api.get("/notifications");
  return res.data;
};

export const markAllNotificationsRead = async () => {
  await api.put("/notifications/read-all");
};

export const markNotificationRead = async (notificationId) => {
  await api.put(`/notifications/${notificationId}/read`);
};

export const getLeaderboard = async (period = "WEEKLY") => {
  const res = await api.get(`/leaderboard/friends?period=${period}`);
  return res.data;
};

export default api;