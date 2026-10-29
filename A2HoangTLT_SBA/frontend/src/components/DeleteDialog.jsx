/**
 * @author HoangTLT
 */

import { useState } from "react";
import { errorMessage } from "../services/api";
import Modal from "./Modal";
import { Alert } from "./ManagementTools";

export default function DeleteDialog({ onDelete, name, onClose, onDeleted }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function remove() {
    setBusy(true);
    setError("");
    try {
      await onDelete();
      onDeleted();
    } catch (e) {
      setError(errorMessage(e));
      setBusy(false);
    }
  }
  return (
    <Modal title="Xác nhận xóa" onClose={onClose} busy={busy}>
      <p>
        Bạn có chắc muốn xóa <strong>{name}</strong>?
      </p>
      <p className="muted">Dữ liệu đã xóa không thể khôi phục.</p>
      <Alert>{error}</Alert>
      <div className="form-actions">
        <button
          className="button button-secondary"
          disabled={busy}
          onClick={onClose}
        >
          Hủy
        </button>
        <button
          className="button button-danger"
          disabled={busy}
          onClick={remove}
        >
          {busy ? "Đang xóa…" : "Xác nhận xóa"}
        </button>
      </div>
    </Modal>
  );
}
