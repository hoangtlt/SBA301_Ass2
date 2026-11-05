/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getPublicNews } from "../services/newsService";
import { errorMessage } from "../services/api";
import { Alert, SearchBar } from "../components/ManagementTools";
import { formatDate } from "../format";

export default function NewsListPage() {
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    // Bỏ qua kết quả tìm kiếm cũ nếu từ khóa đổi hoặc người dùng rời trang.
    let active = true;
    async function loadNews() {
      setLoading(true);
      setError("");
      try {
        const articles = await getPublicNews(query);
        if (active) setRows(articles);
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
  }, [query]);
  return (
    <>
      <h1>Tin tức đại học</h1>
      <p className="muted">Cập nhật các bài viết mới nhất từ nhà trường.</p>
      <section className="news-feed">
        <div className="feed-heading">
          <h2>Bảng tin mới nhất</h2>
          <span>TIN TỨC & CÂU CHUYỆN</span>
        </div>
        <SearchBar
          onSearch={setQuery}
          placeholder="Tìm tiêu đề, nội dung, danh mục hoặc tag…"
          count={rows.length}
          loading={loading}
        />
        <Alert>{error}</Alert>
        {loading ? (
          <p className="empty-state">Đang tải tin tức…</p>
        ) : !error && rows.length === 0 ? (
          <p className="empty-state">Chưa có bài viết phù hợp.</p>
        ) : (
          !error && (
            <div className="news-grid">
              {rows.map((article) => (
                <article
                  className="news-card"
                  key={article.newsArticleId}
                >
                  <div className="news-card-body">
                    <span className="eyebrow">{article.categoryName}</span>
                    <h3>
                      <Link to={`/news/${article.newsArticleId}`}>
                        {article.newsTitle}
                      </Link>
                    </h3>
                    <p>{article.headline}</p>
                    <div className="news-card-footer">
                      <time>{formatDate(article.createdDate)}</time>
                      <Link
                        aria-label={`Đọc bài: ${article.newsTitle}`}
                        to={`/news/${article.newsArticleId}`}
                      >
                        Đọc bài ↗
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )
        )}
      </section>
    </>
  );
}
