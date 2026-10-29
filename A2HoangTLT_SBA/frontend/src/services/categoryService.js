/**
 * @author HoangTLT
 */
import api from "./api";

export async function getCategories(query = "") {
  const response = await api.get("/categories", { params: { q: query } });
  return response.data;
}

export async function createCategory(category) {
  const response = await api.post("/categories", category);
  return response.data;
}

export async function updateCategory(id, category) {
  const response = await api.put(`/categories/${id}`, category);
  return response.data;
}

export async function deleteCategory(id) {
  await api.delete(`/categories/${id}`);
}
