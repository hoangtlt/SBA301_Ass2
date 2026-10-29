/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { getCategories, deleteCategory } from "../services/categoryService";
import { errorMessage } from "../services/api";
import {
  Alert,
  EmptyRow,
  PageHeading,
  SearchBar,
} from "../components/ManagementTools";
import StatusBadge from "../components/StatusBadge";
import CategoryForm from "../components/CategoryForm";
import DeleteDialog from "../components/DeleteDialog";

export default function CategoriesPage() {
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState([]);
  const [allCategories, setAllCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadCount, setReloadCount] = useState(0);
  const [form, setForm] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let active = true;
    async function loadCategories() {
      setLoading(true);
      setError("");
      try {
        const categories = await getCategories(query);
        // Form cần đủ danh mục cha, kể cả khi bảng đang được tìm kiếm.
        const parentOptions = query ? await getCategories() : categories;
        if (active) {
          setRows(categories);
          setAllCategories(parentOptions);
        }
      } catch (error) {
        if (active) setError(errorMessage(error));
      } finally {
        if (active) setLoading(false);
      }
    }
    loadCategories();
    return () => {
      active = false;
    };
  }, [query, reloadCount]);
  function reload() {
    setReloadCount((count) => count + 1);
  }
  return (
    <>
      <PageHeading
        title="Quản lý danh mục"
        description="Sắp xếp tin tức theo chủ đề để người đọc dễ dàng theo dõi."
        action={
          <button
            className="button"
            disabled={loading || !!error}
            onClick={() => {
              setSuccess("");
              setForm({});
            }}
          >
            + Thêm danh mục
          </button>
        }
      />
      <Alert>{error}</Alert>
      <Alert success>{success}</Alert>
      <section className="table-card">
        <SearchBar
          onSearch={setQuery}
          placeholder="Tìm tên hoặc mô tả danh mục…"
          count={rows.length}
          loading={loading}
        />
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Danh mục</th>
                <th>Danh mục cha</th>
                <th>Trạng thái</th>
                <th className="align-right">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading || rows.length === 0 || error ? (
                <EmptyRow columns={5} loading={loading} />
              ) : (
                rows.map((c) => (
                  <tr key={c.categoryId}>
                    <td className="muted">#{c.categoryId}</td>
                    <td>
                      <strong>{c.categoryName}</strong>
                      <small
                        className="table-subtitle description-cell"
                        title={c.categoryDescription}
                      >
                        {c.categoryDescription}
                      </small>
                    </td>
                    <td>{c.parentCategoryName || "—"}</td>
                    <td>
                      <StatusBadge active={c.isActive} />
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          disabled={loading || !!error}
                          onClick={() => {
                            setSuccess("");
                            setForm({ category: c });
                          }}
                        >
                          Sửa
                        </button>
                        <button
                          className="danger-text"
                          onClick={() => {
                            setSuccess("");
                            setDeleting(c);
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
          Danh mục có bài viết hoặc danh mục con cần được giữ lại.
        </p>
      </section>
      {form && (
        <CategoryForm
          category={form.category}
          categories={allCategories}
          onClose={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            setSuccess("Đã lưu danh mục.");
            reload();
          }}
        />
      )}
      {deleting && (
        <DeleteDialog
          onDelete={() => deleteCategory(deleting.categoryId)}
          name={deleting.categoryName}
          onClose={() => setDeleting(null)}
          onDeleted={() => {
            setDeleting(null);
            setSuccess("Đã xóa danh mục.");
            reload();
          }}
        />
      )}
    </>
  );
}
