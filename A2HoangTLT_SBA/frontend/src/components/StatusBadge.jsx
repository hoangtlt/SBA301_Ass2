/**
 * @author HoangTLT
 */

export default function StatusBadge({ active }) {
  return (
    <span className={`badge ${active ? "badge-active" : "badge-inactive"}`}>
      <span className="status-dot" />
      {active ? "Hoạt động" : "Ngừng hiển thị"}
    </span>
  );
}
