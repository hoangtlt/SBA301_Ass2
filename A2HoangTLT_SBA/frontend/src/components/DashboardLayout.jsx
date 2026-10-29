/**
 * @author HoangTLT
 */
import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { errorMessage } from "../services/api";
import { Alert } from "./ManagementTools";

export default function DashboardLayout({ account, onLogout }) {
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const admin = account.accountRole === 1;

  async function signOut() {
    setBusy(true);
    setError("");
    try {
      await onLogout();
      navigate("/login", { replace: true });
    } catch (error) {
      setError(errorMessage(error));
      setBusy(false);
    }
  }

  return (
    <div>
      <header className="site-header">
        <Link to="/" className="brand">FU News</Link>
        <div className="account-menu">
          <span>{account.accountName} ({admin ? "Admin" : "Staff"})</span>
          <button className="button button-secondary" onClick={signOut} disabled={busy}>
            {busy ? "Đang đăng xuất…" : "Đăng xuất"}
          </button>
        </div>
      </header>
      <nav className="management-nav" aria-label="Quản lý">
        {admin ? (
          <NavLink to="/admin/accounts">Tài khoản</NavLink>
        ) : (
          <>
            <NavLink to="/staff/news">Bài viết</NavLink>
            <NavLink to="/staff/categories">Danh mục</NavLink>
            <NavLink to="/staff/history">Lịch sử của tôi</NavLink>
            <NavLink to="/staff/profile">Hồ sơ cá nhân</NavLink>
          </>
        )}
        <Link to="/">Bảng tin công khai</Link>
      </nav>
      <main className="container">
        <Alert>{error}</Alert>
        <Outlet />
      </main>
      <footer className="site-footer">FUNewsManagementSystem · Assignment 02</footer>
    </div>
  );
}
