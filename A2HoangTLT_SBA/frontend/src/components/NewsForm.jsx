/**
 * @author HoangTLT
 */

import { useState } from "react";
import Modal from "./Modal";
import FormField from "./FormField";
import { Alert, FormActions } from "./ManagementTools";
import { createNews, updateNews } from "../services/newsService";
import { errorMessage } from "../services/api";

export default function NewsForm({ article, categories, onClose, onSaved }) {
  const [form, setForm] = useState({
    newsTitle: article?.newsTitle || "",
    headline: article?.headline || "",
    newsContent: article?.newsContent || "",
    newsSource: article?.newsSource || "",
    categoryId: article?.categoryId || "",
    newsStatus: article?.newsStatus ?? 0,
    tags:
      article?.tags.map(({ tagName, note }) => ({
        tagName,
        note: note || "",
      })) || [],
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
      if (article) {
        saved = await updateNews(article.newsArticleId, data);
      } else {
        saved = await createNews(data);
      }
      onSaved(saved);
    } catch (error) {
      setError(errorMessage(error));
      setErrors(error.response?.data?.errors || {});
      setBusy(false);
    }
  }
  const change = (key, value) => setForm({ ...form, [key]: value });
  const changeTag = (index, key, value) =>
    change(
      "tags",
      form.tags.map((tag, i) => (i === index ? { ...tag, [key]: value } : tag)),
    );
  function submit(e) {
    e.preventDefault();
    save({
      ...form,
      newsTitle: form.newsTitle.trim(),
      headline: form.headline.trim(),
      newsContent: form.newsContent.trim(),
      categoryId: Number(form.categoryId),
    });
  }
  const options = categories.filter(
    (c) => c.isActive || c.categoryId === article?.categoryId,
  );
  return (
    <Modal
      title={article ? "Sửa bài viết" : "Thêm bài viết"}
      onClose={onClose}
      busy={busy}
      wide
    >
      <form onSubmit={submit}>
        <Alert>{error}</Alert>
        <fieldset disabled={busy}>
          <FormField label="Tiêu đề *" error={errors.newsTitle}>
            <input
              required
              maxLength={200}
              value={form.newsTitle}
              onChange={(e) => change("newsTitle", e.target.value)}
            />
          </FormField>
          <FormField label="Tóm tắt (Headline) *" error={errors.headline}>
            <textarea
              required
              rows={2}
              maxLength={300}
              value={form.headline}
              onChange={(e) => change("headline", e.target.value)}
            />
          </FormField>
          <div className="form-grid">
            <FormField label="Danh mục *" error={errors.categoryId}>
              <select
                required
                value={form.categoryId}
                onChange={(e) => change("categoryId", e.target.value)}
              >
                <option value="">Chọn danh mục</option>
                {options.map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.categoryName}
                    {!c.isActive ? " (ngừng hiển thị)" : ""}
                  </option>
                ))}
              </select>
            </FormField>
            <FormField label="Trạng thái *" error={errors.newsStatus}>
              <select
                value={form.newsStatus}
                onChange={(e) => change("newsStatus", Number(e.target.value))}
              >
                <option value={0}>Ngừng hiển thị (bản nháp)</option>
                <option value={1}>Hoạt động (công khai)</option>
              </select>
            </FormField>
          </div>
          {options.length === 0 && (
            <Alert>
              Cần tạo một danh mục hoạt động trước khi thêm bài viết.
            </Alert>
          )}
          <FormField label="Nội dung *" error={errors.newsContent}>
            <textarea
              required
              rows={8}
              maxLength={20000}
              value={form.newsContent}
              onChange={(e) => change("newsContent", e.target.value)}
            />
          </FormField>
          <FormField label="Nguồn tin" error={errors.newsSource}>
            <input
              maxLength={300}
              value={form.newsSource}
              onChange={(e) => change("newsSource", e.target.value)}
            />
          </FormField>
          <div className="tags-editor">
            <div className="section-heading">
              <h3>
                Tags <span className="muted">({form.tags.length}/20)</span>
              </h3>
              <button
                type="button"
                className="text-button"
                disabled={form.tags.length >= 20}
                onClick={() =>
                  change("tags", [...form.tags, { tagName: "", note: "" }])
                }
              >
                + Thêm tag
              </button>
            </div>
            <p className="muted small">
              Mỗi tag được lưu riêng cho bài viết này. Tên tag không được trùng
              trong cùng bài.
            </p>
            {form.tags.map((tag, index) => (
              <div className="tag-editor-row" key={index}>
                <FormField
                  label={`Tên tag ${index + 1} *`}
                  error={errors[`tags[${index}].tagName`]}
                >
                  <input
                    required
                    maxLength={50}
                    value={tag.tagName}
                    onChange={(e) =>
                      changeTag(index, "tagName", e.target.value)
                    }
                  />
                </FormField>
                <FormField
                  label={`Ghi chú tag ${index + 1}`}
                  error={errors[`tags[${index}].note`]}
                >
                  <input
                    maxLength={250}
                    value={tag.note}
                    onChange={(e) => changeTag(index, "note", e.target.value)}
                  />
                </FormField>
                <button
                  type="button"
                  className="icon-button danger-text"
                  aria-label={`Xóa tag ${index + 1}`}
                  onClick={() =>
                    change(
                      "tags",
                      form.tags.filter((_, i) => i !== index),
                    )
                  }
                >
                  ×
                </button>
              </div>
            ))}
            {errors.tags && <p className="field-error">{errors.tags}</p>}
          </div>
          <FormActions
            onCancel={onClose}
            busy={busy}
            label={article ? "Lưu thay đổi" : "Tạo bài viết"}
          />
        </fieldset>
      </form>
    </Modal>
  );
}
