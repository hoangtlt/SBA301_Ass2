/**
 * @author HoangTLT
 */
import { useState } from "react";
import { Alert, FormActions } from "./ManagementTools";
import FormField from "./FormField";
import Modal from "./Modal";
import { updateProfile } from "../services/accountService";
import { errorMessage } from "../services/api";

export default function ProfileForm({ account, onClose, onSaved }) {
  const [form, setForm] = useState({
    accountName: account.accountName,
    accountEmail: account.accountEmail,
    currentPassword: "",
    newPassword: "",
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [errors, setErrors] = useState({});

  async function save(data) {
    setBusy(true);
    setError("");
    setErrors({});
    try {
      const account = await updateProfile(data);
      onSaved(account);
    } catch (error) {
      setError(errorMessage(error));
      setErrors(error.response?.data?.errors || {});
      setBusy(false);
    }
  }
  const change = (key, value) => setForm({ ...form, [key]: value });
  return (
    <Modal title="Cập nhật hồ sơ" onClose={onClose} busy={busy}>
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
              value={form.accountEmail}
              onChange={(e) => change("accountEmail", e.target.value)}
            />
          </FormField>
          <FormField label="Mật khẩu hiện tại *" error={errors.currentPassword}>
            <input
              type="password"
              required
              maxLength={72}
              autoComplete="current-password"
              value={form.currentPassword}
              onChange={(e) => change("currentPassword", e.target.value)}
            />
          </FormField>
          <FormField
            label="Mật khẩu mới"
            error={errors.newPassword}
            hint="Để trống nếu không đổi. Mật khẩu mới có ít nhất 6 ký tự và tối đa 72 byte UTF-8."
          >
            <input
              type="password"
              minLength={6}
              maxLength={72}
              autoComplete="new-password"
              value={form.newPassword}
              onChange={(e) => change("newPassword", e.target.value)}
            />
          </FormField>
          <FormActions onCancel={onClose} busy={busy} />
        </fieldset>
      </form>
    </Modal>
  );
}
