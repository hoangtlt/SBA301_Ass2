/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { getCurrentAccount, login, logout } from "./services/authService";
import { setCsrfToken } from "./services/api";
import { Route, Routes, Link } from "react-router-dom";
import PublicLayout from "./components/PublicLayout";
import DashboardLayout from "./components/DashboardLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import AccountsPage from "./pages/AccountsPage";
import CategoriesPage from "./pages/CategoriesPage";

export default function App() {
  // App giữ tài khoản đăng nhập và truyền xuống các trang bằng props.
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    async function restoreSession() {
      try {
        const currentAccount = await getCurrentAccount();
        if (active) setAccount(currentAccount);
      } catch {
        if (active) setAccount(null);
      } finally {
        if (active) setLoading(false);
      }
    }
    function expireSession() {
      setAccount(null);
      setCsrfToken("");
    }
    restoreSession();
    window.addEventListener("session-expired", expireSession);
    return () => {
      active = false;
      window.removeEventListener("session-expired", expireSession);
    };
  }, []);

  async function handleLogin(email, password) {
    const currentAccount = await login(email, password);
    setAccount(currentAccount);
    return currentAccount;
  }

  async function handleLogout() {
    await logout();
    setAccount(null);
  }

  return (
    <Routes>
      <Route element={<PublicLayout account={account} />}>
        <Route index element={<p>FU News</p>} />
        <Route path="news/:id" element={<p>FU News</p>} />
        <Route
          path="login"
          element={
            <LoginPage account={account} loading={loading} onLogin={handleLogin} />
          }
        />
        <Route
          path="*"
          element={
            <div className="not-found">
              <h1>Không tìm thấy trang</h1>
              <Link className="button" to="/">
                Về bảng tin
              </Link>
            </div>
          }
        />
      </Route>
      <Route
        element={<ProtectedRoute role={1} account={account} loading={loading} />}
      >
        <Route
          element={<DashboardLayout account={account} onLogout={handleLogout} />}
        >
          <Route
            path="admin/accounts"
            element={
              <AccountsPage account={account} onAccountChange={setAccount} />
            }
          />
        </Route>
      </Route>
      <Route
        element={<ProtectedRoute role={2} account={account} loading={loading} />}
      >
        <Route
          element={<DashboardLayout account={account} onLogout={handleLogout} />}
        >
          <Route path="staff/news" element={<p>FU News</p>} />
          <Route path="staff/categories" element={<CategoriesPage />} />
          <Route
            path="staff/profile"
            element={
              <p>FU News</p>
            }
          />
        </Route>
      </Route>
    </Routes>
  );
}
