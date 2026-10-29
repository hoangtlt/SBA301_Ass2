/**
 * @author HoangTLT
 */
import api from "./api";

export async function getPublicNews(query = "") {
  const response = await api.get("/public/news", { params: { q: query } });
  return response.data;
}

export async function getPublicNewsById(id) {
  const response = await api.get(`/public/news/${id}`);
  return response.data;
}

export async function getNews(query = "") {
  const response = await api.get("/news", { params: { q: query } });
  return response.data;
}

export async function getNewsHistory(query = "") {
  const response = await api.get("/news/history", { params: { q: query } });
  return response.data;
}

export async function createNews(article) {
  const response = await api.post("/news", article);
  return response.data;
}

export async function updateNews(id, article) {
  const response = await api.put(`/news/${id}`, article);
  return response.data;
}

export async function deleteNews(id) {
  await api.delete(`/news/${id}`);
}
