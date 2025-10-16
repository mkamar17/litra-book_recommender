// import axios from 'axios'

// const api = axios.create({
//   baseURL: '/api',
// })

// export async function testConnection() {
//   try {
//     const res = await api.get('http://localhost:8080/books') // replace with one of your backend endpoints
//     return res.data
//   } catch (err) {
//     console.error('Backend connection failed:', err)
//     return null
//   }
// }
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

export default api;
