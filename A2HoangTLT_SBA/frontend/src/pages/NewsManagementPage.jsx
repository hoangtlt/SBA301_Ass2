/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { getNews, deleteNews } from "../services/newsService";
import { getCategories } from "../services/categoryService";
import { errorMessage } from "../services/api";
import {
  Alert,
  EmptyRow,
  PageHeading,
  SearchBar,
} from "../components/ManagementTools";
import StatusBadge from "../components/StatusBadge";
import NewsForm from "../components/NewsForm";
import DeleteDialog from "../components/DeleteDialog";
import NewsPreview from "../components/NewsPreview";
import { formatDate } from "../format";

export default function NewsManagementPage() {
  const history = false;
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadCount, setReloadCount] = useState(0);
  const [form, setForm] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [preview, setPreview] = useState(null);
  const [success, setSuccess] = useState("");

  function refresh() {
    setReloadCount((count) => count + 1);
  }

  useEffect(() => {
    let active = true;
    async function loadNews() {
      setLoading(true);
      setError("");
      try {
        const articles = await getNews(query);
        const categoryOptions = history ? [] : await getCategories();
        if (active) {
          setRows(articles);
          setCategories(categoryOptions);
        }
      } catch (error) {
        if (active) setError(errorMessage(error));
      } finally {
        if (active) setLoading(false);
      }
    }
    loadNews();
    return () => {
      active = false;
    };
  }, [query, reloadCount, history]);
  return (
    <>
      <PageHeading
        title={history ? "Lịch sử của tôi" : "Quản lý bài viết"}
        description={
          history
            ? "Các bài viết do bạn tạo, bao gồm bài công khai và bản nháp."
            : "Tạo, cập nhật và chia sẻ tin tức với cộng đồng đại học."
        }
        action={
          !history && (
            <button
              className="button"
              disabled={loading || !!error}
              onClick={() => {
                setSuccess("");
                setForm({});
              }}
            >
              + Thêm bài viết
            </button>
          )
        }
      />
      <Alert>{error}</Alert>
      <Alert success>{success}</Alert>
      <section className="table-card">
        <SearchBar
          onSearch={setQuery}
          placeholder="Tìm tiêu đề, nội dung, danh mục hoặc tag…"
          count={rows.length}
          loading={loading}
        />
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Bài viết</th>
                <th>Danh mục</th>
                <th>Trạng thái</th>
                <th>Người tạo / Ngày tạo</th>
                <th className="align-right">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading || rows.length === 0 || error ? (
                <EmptyRow columns={5} loading={loading} />
              ) : (
                rows.map((n) => (
                  <tr key={n.newsArticleId}>
                    <td className="news-title-cell">
                      <button
                        className="title-button"
                        onClick={() => setPreview(n)}
                      >
                        {n.newsTitle}
                      </button>
                      <small className="table-subtitle">
                        #{n.newsArticleId} · {n.tags.length} tags
                      </small>
                    </td>
                    <td>{n.categoryName}</td>
                    <td>
                      <StatusBadge active={n.newsStatus === 1} />
                    </td>
                    <td>
                      {n.createdByName}
                      <small className="table-subtitle">
                        {formatDate(n.createdDate)}
                      </small>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button onClick={() => setPreview(n)}>Xem</button>
                        {!history && (
                          <>
                            <button
                              disabled={
                                loading || !!error
                              }
                              onClick={() => {
                                setSuccess("");
                                setForm({ article: n });
                              }}
                            >
                              Sửa
                            </button>
                            <button
                              className="danger-text"
                              onClick={() => {
                                setSuccess("");
                                setDeleting(n);
                              }}
                            >
                              Xóa
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
      {form && (
        <NewsForm
          article={form.article}
          categories={categories}
          onClose={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            setSuccess("Đã lưu bài viết.");
            refresh();
          }}
        />
      )}
      {deleting && (
        <DeleteDialog
          onDelete={() => deleteNews(deleting.newsArticleId)}
          name={deleting.newsTitle}
          onClose={() => setDeleting(null)}
          onDeleted={() => {
            setDeleting(null);
            setSuccess("Đã xóa bài viết.");
            refresh();
          }}
        />
      )}
      {preview && (
        <NewsPreview article={preview} onClose={() => setPreview(null)} />
      )}
    </>
  );
}
