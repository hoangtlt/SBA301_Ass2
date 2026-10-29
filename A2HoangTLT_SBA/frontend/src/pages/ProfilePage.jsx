/**
 * @author HoangTLT
 */
import { useState } from "react";
import { PageHeading, Alert } from "../components/ManagementTools";
import ProfileForm from "../components/ProfileForm";

export default function ProfilePage({ account, onAccountChange }) {
  const [editing, setEditing] = useState(false);
  const [success, setSuccess] = useState("");
  return (
    <>
      <PageHeading
        title="Hồ sơ cá nhân"
        description="Thông tin tài khoản và mật khẩu của bạn."
      />
      <Alert success>{success}</Alert>
      <section className="profile-card">
        <div className="profile-body">
          <h2>{account.accountName}</h2>
          <span className="badge badge-staff">Staff</span>
          <dl className="profile-details">
            <div>
              <dt>Mã tài khoản</dt>
              <dd>#{account.accountId}</dd>
            </div>
            <div>
              <dt>Email</dt>
              <dd>{account.accountEmail}</dd>
            </div>
            <div>
              <dt>Vai trò</dt>
              <dd>Nhân viên biên tập (Staff)</dd>
            </div>
          </dl>
          <button
            className="button"
            onClick={() => {
              setSuccess("");
              setEditing(true);
            }}
          >
            Cập nhật hồ sơ
          </button>
        </div>
      </section>
      {editing && (
        <ProfileForm
          account={account}
          onClose={() => setEditing(false)}
          onSaved={(data) => {
            onAccountChange(data);
            setEditing(false);
            setSuccess("Đã cập nhật hồ sơ.");
          }}
        />
      )}
    </>
  );
}
