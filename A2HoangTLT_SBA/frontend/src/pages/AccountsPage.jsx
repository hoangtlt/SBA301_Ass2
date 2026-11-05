/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { getAccounts, deleteAccount } from "../services/accountService";
import { errorMessage } from "../services/api";
import {
  Alert,
  EmptyRow,
  PageHeading,
  SearchBar,
} from "../components/ManagementTools";
import AccountForm from "../components/AccountForm";
import DeleteDialog from "../components/DeleteDialog";

export default function AccountsPage({ account, onAccountChange }) {
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadCount, setReloadCount] = useState(0);
  const [form, setForm] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [success, setSuccess] = useState("");

  function refresh() {
    setReloadCount((count) => count + 1);
  }

  useEffect(() => {
    let active = true;
    async function loadAccounts() {
      setLoading(true);
      setError("");
      try {
        const accounts = await getAccounts(query);
        if (active) setRows(accounts);
      } catch (error) {
        if (active) setError(errorMessage(error));
      } finally {
        if (active) setLoading(false);
      }
    }
    loadAccounts();
    // Bỏ qua kết quả cũ nếu chuyển trang hoặc đổi từ khóa trước khi tải xong.
    return () => {
      active = false;
    };
  }, [query, reloadCount]);
  function saved(value) {
    // Sửa chính mình thì đồng bộ account ở App để tên trên menu đổi ngay.
    if (value.accountId === account.accountId) onAccountChange(value);
    setForm(null);
    setSuccess("Đã lưu tài khoản.");
    refresh();
  }
  return (
    <>
      <PageHeading
        title="Quản lý tài khoản"
        description="Quản lý thông tin và quyền truy cập của đội ngũ biên tập."
        action={
          <button
            className="button"
            onClick={() => {
              setSuccess("");
              setForm({});
            }}
          >
            + Thêm tài khoản
          </button>
        }
      />
      <Alert>{error}</Alert>
      <Alert success>{success}</Alert>
      <section className="table-card">
        <SearchBar
          onSearch={setQuery}
          placeholder="Tìm theo tên hoặc email…"
          count={rows.length}
          loading={loading}
        />
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tài khoản</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th className="align-right">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading || rows.length === 0 || error ? (
                <EmptyRow columns={5} loading={loading} />
              ) : (
                rows.map((a) => (
                  <tr key={a.accountId}>
                    <td className="muted">#{a.accountId}</td>
                    <td>
                      <strong>{a.accountName}</strong>
                      {a.accountId === account.accountId && (
                        <small className="table-subtitle">
                          Tài khoản của bạn
                        </small>
                      )}
                    </td>
                    <td>{a.accountEmail}</td>
                    <td>
                      <span
                        className={`badge ${a.accountRole === 1 ? "badge-admin" : "badge-staff"}`}
                      >
                        {a.accountRole === 1 ? "Admin" : "Staff"}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          onClick={() => {
                            setSuccess("");
                            setForm({ account: a });
                          }}
                        >
                          Sửa
                        </button>
                        <button
                          className="danger-text"
                          onClick={() => {
                            setSuccess("");
                            setDeleting(a);
                          }}
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <p className="table-note">
          Chỉ có thể xóa tài khoản chưa tạo bài viết.
        </p>
      </section>
      {form && (
        <AccountForm
          account={form.account}
          onClose={() => setForm(null)}
          onSaved={saved}
        />
      )}
      {deleting && (
        <DeleteDialog
          onDelete={() => deleteAccount(deleting.accountId)}
          name={deleting.accountName}
          onClose={() => setDeleting(null)}
          onDeleted={() => {
            setDeleting(null);
            setSuccess("Đã xóa tài khoản.");
            refresh();
          }}
        />
      )}
    </>
  );
}
