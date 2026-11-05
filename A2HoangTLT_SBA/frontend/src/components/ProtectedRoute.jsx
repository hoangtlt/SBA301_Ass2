/**
 * @author HoangTLT
 */

import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function ProtectedRoute({ role, account, loading }) {
  const location = useLocation();
  // Chờ khôi phục phiên trước khi quyết định chuyển hướng để tránh về login nhầm.
  if (loading)
    return <p className="loading-page">Đang kiểm tra phiên đăng nhập…</p>;
  // Lưu trang được yêu cầu để quay lại sau khi đăng nhập đúng vai trò.
  if (!account)
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (account.accountRole !== role)
    return (
      <Navigate
        to={account.accountRole === 1 ? "/admin/accounts" : "/staff/news"}
        replace
      />
    );
  return <Outlet />;
}
