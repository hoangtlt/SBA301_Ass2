/**
 * @author HoangTLT
 */

import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { errorMessage } from "../services/api";
import FormField from "../components/FormField";
import { Alert } from "../components/ManagementTools";

export default function LoginPage({ account, loading, onLogin }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  if (loading) return <p className="loading-page">Đang tải…</p>;
  if (account)
    return (
      <Navigate
        replace
        to={account.accountRole === 1 ? "/admin/accounts" : "/staff/news"}
      />
    );
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const user = await onLogin(email.trim(), password);
      const destination =
        user.accountRole === 1 ? "/admin/accounts" : "/staff/news";
      const requested = location.state?.from;
      // Chỉ quay lại đường dẫn thuộc vai trò vừa đăng nhập; còn lại về trang mặc định.
      navigate(
        requested?.startsWith(user.accountRole === 1 ? "/admin/" : "/staff/")
          ? requested
          : destination,
        { replace: true },
      );
    } catch (e) {
      setError(errorMessage(e));
      setBusy(false);
    }
  }
  return (
    <section className="login-section">
      <div className="login-card">
        <p className="eyebrow">CHÀO MỪNG TRỞ LẠI</p>
        <h2>Đăng nhập</h2>
        <p className="muted">Sử dụng tài khoản được cấp để tiếp tục.</p>
        <Alert>{error}</Alert>
        <form onSubmit={submit}>
          <fieldset disabled={busy}>
            <FormField label="Email">
              <input
                type="email"
                required
                maxLength={255}
                autoComplete="username"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ban@funews.com"
              />
            </FormField>
            <FormField label="Mật khẩu">
              <input
                type="password"
                required
                maxLength={72}
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Nhập mật khẩu"
              />
            </FormField>
            <button className="button full-width" type="submit">
              {busy ? "Đang đăng nhập…" : "Đăng nhập →"}
            </button>
          </fieldset>
        </form>
        <p className="login-note">
          Bạn vẫn có thể đọc tin tức công khai mà không cần tài khoản.
        </p>
      </div>
    </section>
  );
}
