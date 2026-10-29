/**
 * @author HoangTLT
 */

import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function ProtectedRoute({ role, account, loading }) {
  const location = useLocation();
  if (loading)
    return <p className="loading-page">Đang kiểm tra phiên đăng nhập…</p>;
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
