/**
 * @author HoangTLT
 */

import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { errorMessage } from "../services/api";
import { getPublicNewsById } from "../services/newsService";
import { Alert } from "../components/ManagementTools";
import { formatDate } from "../format";

export default function NewsDetailPage() {
  const { id } = useParams();
  const [article, setArticle] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let active = true;
    async function loadArticle() {
      setLoading(true);
      setError("");
      try {
        const article = await getPublicNewsById(id);
        if (active) setArticle(article);
      } catch (error) {
        if (active) setError(errorMessage(error));
      } finally {
        if (active) setLoading(false);
      }
    }
    loadArticle();
    return () => {
      active = false;
    };
  }, [id]);
  return (
    <div className="article-page">
      <Link to="/" className="back-link">
        ← Trở lại bảng tin
      </Link>
      <Alert>{error}</Alert>
      {loading ? (
        <p>Đang tải bài viết…</p>
      ) : (
        !error &&
        article && (
          <article>
            <p className="eyebrow">{article.categoryName}</p>
            <h1>{article.newsTitle}</h1>
            <p className="article-headline">{article.headline}</p>
            <div className="article-meta">
              <span>{article.createdByName}</span>
              <time>{formatDate(article.createdDate)}</time>
            </div>
            <div className="article-content">{article.newsContent}</div>
            {article.newsSource && (
              <p className="muted">Nguồn: {article.newsSource}</p>
            )}
            <div className="tags">
              {article.tags.map((tag) => (
                <span className="tag" key={tag.tagId}>
                  #{tag.tagName}
                </span>
              ))}
            </div>
            {article.modifiedDate && (
              <p className="muted">
                Cập nhật: {formatDate(article.modifiedDate)} ·{" "}
                {article.updatedByName || "Staff"}
              </p>
            )}
          </article>
        )
      )}
    </div>
  );
}
