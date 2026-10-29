/**
 * @author HoangTLT
 */
import api, { setCsrfToken } from "./api";

export async function getCurrentAccount() {
  const response = await api.get("/auth/me");
  setCsrfToken(response.data.csrfToken);
  return response.data.account;
}

export async function login(email, password) {
  const response = await api.post("/auth/login", { email, password });
  setCsrfToken(response.data.csrfToken);
  return response.data.account;
}

export async function logout() {
  try {
    await api.post("/auth/logout");
  } catch (error) {
    // Phiên đã hết hạn cũng được xem như đã đăng xuất.
    if (error.response?.status !== 401) throw error;
  }
  setCsrfToken("");
}
