/**
 * @author HoangTLT
 */
import { Link, Outlet } from "react-router-dom";

export default function PublicLayout({ account }) {
  let managementUrl = "/login";
  if (account) {
    managementUrl = account.accountRole === 1 ? "/admin/accounts" : "/staff/news";
  }

  return (
    <div>
      <header className="site-header">
        <Link to="/" className="brand">FU News</Link>
        <nav aria-label="Điều hướng chính">
          <Link to="/">Bảng tin</Link>
          <Link to={managementUrl}>
            {account ? "Trang quản lý" : "Đăng nhập"}
          </Link>
        </nav>
      </header>
      <main className="container"><Outlet /></main>
      <footer className="site-footer">FUNewsManagementSystem · Assignment 02</footer>
    </div>
  );
}
