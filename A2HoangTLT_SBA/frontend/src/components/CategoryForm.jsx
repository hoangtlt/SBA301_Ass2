/**
 * @author HoangTLT
 */

import { useState } from "react";
import Modal from "./Modal";
import FormField from "./FormField";
import { Alert, FormActions } from "./ManagementTools";
import { createCategory, updateCategory } from "../services/categoryService";
import { errorMessage } from "../services/api";

export default function CategoryForm({
  category,
  categories,
  onClose,
  onSaved,
}) {
  const [form, setForm] = useState({
    categoryName: category?.categoryName || "",
    categoryDescription: category?.categoryDescription || "",
    parentCategoryId: category?.parentCategoryId || "",
    isActive: category?.isActive ?? true,
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
      if (category) {
        saved = await updateCategory(category.categoryId, data);
      } else {
        saved = await createCategory(data);
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
      title={category ? "Sửa danh mục" : "Thêm danh mục"}
      onClose={onClose}
      busy={busy}
    >
      <form
        onSubmit={(e) => {
          e.preventDefault();
          save({
            ...form,
            categoryName: form.categoryName.trim(),
            categoryDescription: form.categoryDescription.trim(),
            parentCategoryId: form.parentCategoryId
              ? Number(form.parentCategoryId)
              : null,
          });
        }}
      >
        <Alert>{error}</Alert>
        <fieldset disabled={busy}>
          <FormField label="Tên danh mục *" error={errors.categoryName}>
            <input
              required
              maxLength={100}
              value={form.categoryName}
              onChange={(e) => change("categoryName", e.target.value)}
            />
          </FormField>
          <FormField label="Mô tả *" error={errors.categoryDescription}>
            <textarea
              required
              rows={4}
              maxLength={500}
              value={form.categoryDescription}
              onChange={(e) => change("categoryDescription", e.target.value)}
            />
          </FormField>
          <FormField label="Danh mục cha" error={errors.parentCategoryId}>
            <select
              value={form.parentCategoryId}
              onChange={(e) => change("parentCategoryId", e.target.value)}
            >
              <option value="">Không có danh mục cha</option>
              {categories
                .filter((c) => c.categoryId !== category?.categoryId)
                .map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.categoryName}
                  </option>
                ))}
            </select>
          </FormField>
          <FormField label="Trạng thái *" error={errors.isActive}>
            <select
              value={String(form.isActive)}
              onChange={(e) => change("isActive", e.target.value === "true")}
            >
              <option value="true">Hoạt động</option>
              <option value="false">Ngừng hiển thị</option>
            </select>
          </FormField>
          <FormActions
            onCancel={onClose}
            busy={busy}
            label={category ? "Lưu thay đổi" : "Tạo danh mục"}
          />
        </fieldset>
      </form>
    </Modal>
  );
}
