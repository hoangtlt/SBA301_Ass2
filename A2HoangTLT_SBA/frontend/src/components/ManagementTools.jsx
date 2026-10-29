/**
 * @author HoangTLT
 */

import { useState } from "react";

export function PageHeading({
  eyebrow = "KHÔNG GIAN LÀM VIỆC",
  title,
  description,
  action,
}) {
  return (
    <div className="page-heading">
      <div>
        <h1>{title}</h1>
        <p className="muted">{description}</p>
      </div>
      {action}
    </div>
  );
}
export function SearchBar({
  onSearch,
  placeholder = "Tìm kiếm…",
  count,
  loading,
}) {
  const [text, setText] = useState("");
  return (
    <div className="table-toolbar">
      <form
        className="search-form"
        onSubmit={(e) => {
          e.preventDefault();
          onSearch(text.trim());
        }}
      >
        <input
          type="search"
          aria-label="Tìm kiếm"
          placeholder={placeholder}
          value={text}
          onChange={(e) => setText(e.target.value)}
          maxLength={200}
        />
        <button className="button button-secondary" type="submit">
          Tìm kiếm
        </button>
        {text && (
          <button
            type="button"
            className="text-button"
            onClick={() => {
              setText("");
              onSearch("");
            }}
          >
            Xóa bộ lọc
          </button>
        )}
      </form>
      <span className="muted result-count" aria-live="polite">
        {loading ? "Đang tải…" : `${count} kết quả`}
      </span>
    </div>
  );
}
export function Alert({ children, success = false }) {
  return children ? (
    <div
      role={success ? "status" : "alert"}
      className={`alert ${success ? "alert-success" : ""}`}
    >
      {children}
    </div>
  ) : null;
}
export function EmptyRow({ columns, loading }) {
  return (
    <tr>
      <td colSpan={columns} className="empty-state">
        {loading ? "Đang tải dữ liệu…" : "Chưa có dữ liệu phù hợp."}
      </td>
    </tr>
  );
}
export function FormActions({ onCancel, busy, label = "Lưu thay đổi" }) {
  return (
    <div className="form-actions">
      <button
        type="button"
        className="button button-secondary"
        disabled={busy}
        onClick={onCancel}
      >
        Hủy
      </button>
      <button type="submit" className="button" disabled={busy}>
        {busy ? "Đang lưu…" : label}
      </button>
    </div>
  );
}
