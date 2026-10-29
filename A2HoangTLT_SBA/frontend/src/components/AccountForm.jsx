/**
 * @author HoangTLT
 */

import { useState } from "react";
import Modal from "./Modal";
import FormField from "./FormField";
import { Alert, FormActions } from "./ManagementTools";
import { createAccount, updateAccount } from "../services/accountService";
import { errorMessage } from "../services/api";

export default function AccountForm({ account, onClose, onSaved }) {
  const [form, setForm] = useState({
    accountName: account?.accountName || "",
    accountEmail: account?.accountEmail || "",
    accountRole: account?.accountRole || 2,
    password: "",
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [errors, setErrors] = useState({});

  async function save(data) {
    setBusy(true);
    setError("");
    setErrors({});
    try {
      let saved;
      if (account) {
        saved = await updateAccount(account.accountId, data);
      } else {
        saved = await createAccount(data);
      }
      onSaved(saved);
    } catch (error) {
      setError(errorMessage(error));
      setErrors(error.response?.data?.errors || {});
      setBusy(false);
    }
  }
  const change = (key, value) => setForm({ ...form, [key]: value });
  return (
    <Modal
      title={account ? "Sửa tài khoản" : "Thêm tài khoản"}
      onClose={onClose}
      busy={busy}
    >
      <form
        onSubmit={(e) => {
          e.preventDefault();
          save({
            ...form,
            accountName: form.accountName.trim(),
            accountEmail: form.accountEmail.trim(),
          });
        }}
      >
        <Alert>{error}</Alert>
        <fieldset disabled={busy}>
          <FormField label="Họ và tên *" error={errors.accountName}>
            <input
              required
              maxLength={100}
              value={form.accountName}
              onChange={(e) => change("accountName", e.target.value)}
            />
          </FormField>
          <FormField label="Email *" error={errors.accountEmail}>
            <input
              type="email"
              required
              maxLength={255}
              autoComplete="off"
              value={form.accountEmail}
              onChange={(e) => change("accountEmail", e.target.value)}
            />
          </FormField>
          <FormField label="Vai trò *" error={errors.accountRole}>
            <select
              value={form.accountRole}
              onChange={(e) => change("accountRole", Number(e.target.value))}
            >
              <option value={1}>Admin</option>
              <option value={2}>Staff</option>
            </select>
          </FormField>
          <FormField
            label={account ? "Mật khẩu mới" : "Mật khẩu *"}
            error={errors.password}
            hint={
              account
                ? "Để trống để giữ mật khẩu hiện tại. Mật khẩu mới có ít nhất 6 ký tự."
                : "Ít nhất 6 ký tự, tối đa 72 byte UTF-8."
            }
          >
            <input
              type="password"
              autoComplete="new-password"
              required={!account}
              minLength={6}
              maxLength={72}
              value={form.password}
              onChange={(e) => change("password", e.target.value)}
            />
          </FormField>
          <FormActions
            onCancel={onClose}
            busy={busy}
            label={account ? "Lưu thay đổi" : "Tạo tài khoản"}
          />
        </fieldset>
      </form>
    </Modal>
  );
}
