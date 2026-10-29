/**
 * @author HoangTLT
 */

import Modal from "./Modal";
import StatusBadge from "./StatusBadge";
import { formatDate } from "../format";

export default function NewsPreview({ article, onClose }) {
  return (
    <Modal title="Chi tiết bài viết" onClose={onClose} wide>
      <article className="article-preview">
        <div className="preview-meta">
          <span className="eyebrow">{article.categoryName}</span>
          <StatusBadge active={article.newsStatus === 1} />
        </div>
        <h2>{article.newsTitle}</h2>
        <p className="article-headline">{article.headline}</p>
        <p className="muted">
          Tác giả: {article.createdByName} · {formatDate(article.createdDate)}
        </p>
        <div className="article-content">{article.newsContent}</div>
        {article.newsSource && <p>Nguồn: {article.newsSource}</p>}
        <div className="tags">
          {article.tags.map((tag) => (
            <span className="tag" title={tag.note} key={tag.tagId}>
              #{tag.tagName}
              {tag.note && <small> · {tag.note}</small>}
            </span>
          ))}
        </div>
        {article.modifiedDate && (
          <p className="muted">
            Sửa bởi: {article.updatedByName || "Staff"} ·{" "}
            {formatDate(article.modifiedDate)}
          </p>
        )}
      </article>
    </Modal>
  );
}
