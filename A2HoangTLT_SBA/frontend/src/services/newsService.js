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
