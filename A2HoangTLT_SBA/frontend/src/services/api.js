/**
 * @author HoangTLT
 */

import axios from "axios";

const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL || ""}/api`,
  withCredentials: true,
  timeout: 15000,
});

let csrfToken = "";
export function setCsrfToken(value) {
  csrfToken = value || "";
}

api.interceptors.request.use((config) => {
  // Cookie xác định phiên; CSRF token xác nhận các thao tác thay đổi dữ liệu.
  if (csrfToken && ["post", "put", "delete"].includes(config.method)) {
    config.headers["X-CSRF-Token"] = csrfToken;
  }
  return config;
});
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Không coi 401 khi kiểm tra phiên/đăng nhập là sự kiện hết hạn phiên đang sử dụng.
    if (
      error.response?.status === 401 &&
      !["/auth/login", "/auth/me"].includes(error.config?.url)
    ) {
      window.dispatchEvent(new Event("session-expired"));
    }
    return Promise.reject(error);
  },
);

export function errorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message;
  return "Không kết nối được máy chủ. Hãy kiểm tra BE đang chạy tại cổng 8080.";
}
export default api;
