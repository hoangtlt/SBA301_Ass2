/**
 * @author HoangTLT
 */
import api from "./api";

// api.js tự thêm /api trước các đường dẫn bên dưới.
export async function getAccounts(query = "") {
  const response = await api.get("/accounts", { params: { q: query } });
  return response.data;
}

export async function createAccount(account) {
  const response = await api.post("/accounts", account);
  return response.data;
}

export async function updateAccount(id, account) {
  const response = await api.put(`/accounts/${id}`, account);
  return response.data;
}

export async function deleteAccount(id) {
  await api.delete(`/accounts/${id}`);
}
