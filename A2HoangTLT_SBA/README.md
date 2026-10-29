# Assignment 02 - FUNewsManagementSystem

Thư mục tổng: **A2HoangTLT_SBA**, gồm hai thư mục mã nguồn **backend** và **frontend**. Sinh viên / lớp: **HoangTLT_SBA301**. Database và mã ứng dụng Maven: **A2HoangTLT_SBA301**.

Ứng dụng quản lý tin tức cho Assignment 02 môn SBA301. Backend dùng **Spring Boot 3 + Spring Data JPA + SQL Server**, frontend dùng **React + Axios**. Code tổ chức theo các tầng cơ bản, dùng JavaScript và CSS thuần ở FE.

## 1. Cấu trúc

```text
A2HoangTLT_SBA/
├── backend/
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd, .mvn/           # Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/funews/
│       │   │   ├── controller/         # Nhận HTTP request, trả response
│       │   │   ├── service/            # Xử lý nghiệp vụ
│       │   │   ├── repository/         # Truy cập dữ liệu bằng JPA
│       │   │   ├── entity/             # Ánh xạ bảng SQL Server
│       │   │   ├── dto/                # Dữ liệu đầu vào / đầu ra
│       │   │   ├── config/             # CORS, mật khẩu, dữ liệu mẫu
│       │   │   └── exception/          # Trả lỗi chung
│       │   └── resources/application.properties
│       └── test/                       # Kiểm thử API tích hợp
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── tests/                          # Kiểm thử trình duyệt
│   └── src/
│       ├── components/                 # Popup, form, khung trang và menu
│       ├── pages/                      # Màn hình, state và useEffect tải dữ liệu
│       ├── services/                   # Axios và các hàm API theo chức năng
│       ├── App.jsx                     # Routes, đăng nhập và truyền props
│       ├── main.jsx                    # Khởi động React
│       ├── styles.css                  # CSS cơ bản, hỗ trợ điện thoại
│       └── format.js                   # Định dạng ngày giờ
└── scripts/                            # Tạo database và chạy dự án bằng PowerShell
```

Luồng xử lý: **React → Axios → Controller → Service → Repository → SQL Server**. Controller không gọi thẳng Repository.

FE được tổ chức đơn giản để dễ học: page/form gọi các hàm rõ tên như `getAccounts`, `createAccount`, `updateAccount`, `deleteAccount` trong `services/accountService.js`. Các chức năng danh mục, tin tức và đăng nhập có service tương ứng. Các form tự xử lý `async/await`, trạng thái lưu và lỗi; các page tự tải danh sách bằng `useEffect`. `App.jsx` giữ tài khoản đăng nhập và truyền qua props.

## 2. Chuẩn bị

- JDK **17 trở lên**; đã kiểm tra trên JDK 21.
- Node.js **22.12 trở lên**; đã kiểm tra trên Node 22.23.2.
- SQL Server, bật TCP/IP và biết cổng kết nối.
- Không cần cài Maven riêng: dự án có Maven Wrapper. Lần đầu cần Internet để tải thư viện.
- Mở thư mục `backend` trong IntelliJ IDEA để làm việc với BE; bật annotation processing cho Lombok nếu IDE báo không nhận getter/setter. Lombok chỉ tạo getter, setter và constructor rỗng.

## 3. Chạy nhanh trên máy hiện tại (Windows Authentication)

SQL Server trên máy hiện tại có instance **`.\SQL2022`**, cổng TCP **1433**. Mở PowerShell tại thư mục `A2HoangTLT_SBA` (từ `Ass2`, chạy `cd .\A2HoangTLT_SBA`).

**Bước 1: Tạo database nếu chưa có.**

```powershell
.\scripts\Setup-Database.ps1
```

Script chỉ tạo database `A2HoangTLT_SBA301` nếu chưa tồn tại. Khi BE chạy, JPA tạo các bảng và quan hệ theo entity. Nếu dùng SSMS, có thể chạy `CREATE DATABASE A2HoangTLT_SBA301;` thay cho script.

**Bước 2: Chạy backend ở terminal thứ nhất.**

```powershell
.\scripts\Start-Backend.ps1
```

Script tải DLL Windows Authentication chính thức từ Maven Central vào thư mục `native` bị bỏ qua bởi Git, sau đó chạy BE với tài khoản Windows hiện tại. Cần Windows x64 và quyền truy cập SQL Server. API: **http://localhost:8080/api/public/news**.

**Bước 3: Chạy frontend ở terminal thứ hai.**

```powershell
.\scripts\Start-Frontend.ps1
```

Script chạy `npm ci` nếu chưa có thư viện, sau đó chạy Vite. Mở **http://localhost:5173**. Dùng đúng `localhost` để cookie đăng nhập hoạt động nhất quán. Nhấn `Ctrl+C` trong từng terminal để dừng.

Nếu PowerShell chặn chạy script, có thể dùng cho từng lệnh:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Start-Backend.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\Start-Frontend.ps1
```

## 4. Chạy bằng SQL Server Authentication / trên máy khác

Tạo database bằng SSMS, rồi cấu hình trong terminal chạy BE:

```powershell
cd backend
$env:DB_URL='jdbc:sqlserver://localhost:1433;databaseName=A2HoangTLT_SBA301;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME='sa'
$env:DB_PASSWORD='mat_khau_SQL_Server_cua_ban'
.\mvnw.cmd spring-boot:run
```

Thay host, cổng, username và password theo SQL Server của bạn. Các giá trị mặc định nằm trong `src/main/resources/application.properties`; không ghi mật khẩu thật vào mã nguồn. Spring không tự đọc file `.env` của BE, nên dùng biến môi trường như trên hoặc cấu hình Run Configuration của IntelliJ.

Máy phát triển cũng có thể dùng `backend/src/main/resources/application-local.properties` để giữ cấu hình riêng. File này được Spring tự nạp nếu có và được Git bỏ qua. Khi clone dự án trên máy khác, cấu hình `DB_PASSWORD` trước khi chạy bằng SQL Server Authentication.

FE chạy thủ công:

```powershell
cd frontend
npm ci
npm run dev
```

Vite chuyển tiếp `/api` sang cổng 8080, nên không phải sửa từng lời gọi Axios. Nếu đổi cổng BE, sửa `vite.config.js`. Nếu FE gọi trực tiếp sang host khác, đặt `VITE_API_URL` theo `.env.example` và đặt `FRONTEND_URL` ở BE đúng địa chỉ FE.

## 5. Tài khoản mẫu

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Admin | `admin@funews.com` | `Admin@123` |
| Staff | `staff@funews.com` | `Staff@123` |
| Staff thứ hai | `staff2@funews.com` | `Staff@123` |

Dữ liệu mẫu chỉ được tạo khi database hoàn toàn trống: 3 tài khoản, 3 danh mục, 2 tin công khai và 1 bản nháp. Khởi động lại không ghi đè dữ liệu đã sửa. Đặt `SEED_DATA=false` để tắt tạo mẫu. Mật khẩu được lưu dưới dạng BCrypt, API không trả mật khẩu hoặc mã băm.

## 6. Chức năng và cách thử

1. Chưa đăng nhập: xem bảng tin, tìm kiếm, mở chi tiết. Bản nháp không hiển thị, kể cả truy cập trực tiếp bằng ID.
2. Admin: thêm/sửa tài khoản bằng popup; tìm tên hoặc email; xóa luôn yêu cầu xác nhận. Xóa Staff đã tạo bài viết sẽ báo lỗi.
3. Staff → Danh mục: thêm/sửa bằng popup; chọn danh mục cha và trạng thái; tìm theo tên/mô tả. Không xóa được danh mục có bài viết hoặc danh mục con.
4. Staff → Bài viết: tạo bài, nhập tag và ghi chú, lưu bản nháp hoặc công khai. Có thể xem, sửa, xóa và tìm theo tiêu đề/nội dung/danh mục/tag.
5. Staff → Lịch sử của tôi: chỉ hiện những bài do tài khoản hiện tại tạo. `staff2@funews.com` ban đầu có lịch sử trống.
6. Staff → Hồ sơ cá nhân: sửa tên/email, đổi mật khẩu qua popup; phải nhập đúng mật khẩu hiện tại. Vai trò không sửa được trong hồ sơ.
7. Đăng xuất và đăng nhập lại để kiểm tra thông tin mới.

API quản trị dùng `/api/accounts`, `/api/categories`, `/api/news`, `/api/news/history` và `/api/profile`. Khách xem tin qua `/api/public/news`. Các endpoint danh sách nhận tham số tìm kiếm `q`; xác thực dùng `/api/auth/login`, `/api/auth/me` và `/api/auth/logout`.

## 7. Những phần đề chưa quy định rõ

- ID dùng số nguyên tự tăng vì sơ đồ không chỉ định kiểu dữ liệu.
- Quyền: Admin quản lý tài khoản; Staff quản lý toàn bộ danh mục và bài viết. Lịch sử chỉ lọc bài do mình tạo. API kiểm tra quyền ở BE, FE bảo vệ các route tương ứng.
- Đăng nhập dùng HTTP session, cookie HttpOnly và token CSRF, không cần JWT hay dịch vụ ngoài. Cookie hết hạn sau 30 phút không hoạt động; BE khởi động lại thì cần đăng nhập lại.
- Sơ đồ có bảng `NewsTag`, còn mô tả giới hạn một tag thuộc tối đa một bài. Giữ bảng nối với khóa chính kép và unique `TagID`; tag được thêm/sửa/xóa trong popup bài viết. Hai bài được dùng cùng tên tag nhưng là hai bản ghi riêng.
- Danh mục không được làm cha của chính mình hoặc tạo vòng lặp. Không cho chọn danh mục ngừng hoạt động khi tạo bài hay công khai bài. Tin đã công khai vẫn được lọc theo `NewsStatus` như đề yêu cầu.
- Không tự xóa tài khoản đang đăng nhập hoặc làm mất Admin cuối cùng. Nếu một tài khoản chỉ sửa bài mà chưa tạo bài, vẫn cho xóa và đặt `UpdatedByID` về null.
- Ngày tạo, người tạo, ngày sửa và người sửa do server tự ghi. Nội dung dùng văn bản thuần để dễ học và tránh chèn HTML.
- Kiểm tra bắt buộc, độ dài, email, role 1/2, status 0/1, ID tham chiếu và tag; FE có kiểm tra form và BE kiểm tra lại bằng Bean Validation / service.
- Phần giới thiệu nhắc lịch xuất bản, analytics và duyệt bài nhưng mục chức năng không yêu cầu; phạm vi triển khai theo mục Main Functions của đề.

## 8. Kiểm thử và build

Backend (H2 chỉ dùng trong test; ứng dụng chạy thật vẫn dùng SQL Server):

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd package
```

Frontend:

```powershell
cd frontend
npm run build
# Cần BE :8080, FE :5173 và Chrome đã cài; chạy trên dữ liệu mẫu chưa sửa.
npm run test:e2e
```

Playwright mặc định dùng Chrome có sẵn. Nếu dùng Chromium của Playwright: chạy `npx playwright install chromium`, đặt `$env:PLAYWRIGHT_CHANNEL='chromium'`, rồi chạy test. Browser test tạo dữ liệu riêng và dọn dẹp sau mỗi trường hợp; ảnh chụp được tạo cục bộ trong `docs/evidence`.

Kết quả kiểm tra FE sau khi đơn giản hóa cấu trúc: **5/5 kiểm thử trình duyệt với SQL Server thật**, build FE thành công. Kiểm thử gồm khôi phục phiên sau khi tải lại trang, phiên hết hạn, phân quyền, CRUD, điều kiện chặn xóa, tag, lịch sử, hồ sơ và giao diện điện thoại. Bộ 9 kiểm thử BE được giữ nguyên; đợt thay đổi này tập trung vào FE.

`npm run build` tạo FE trong `dist/`. Khi triển khai riêng, cần web server hỗ trợ trả `index.html` cho các route React và reverse proxy `/api` về BE; Vite dev proxy chỉ phục vụ lúc phát triển.

## 9. Xử lý lỗi thường gặp

| Lỗi | Cách kiểm tra |
| --- | --- |
| Không kết nối SQL Server | Kiểm tra dịch vụ SQL Server, TCP/IP, host/cổng, database và quyền của tài khoản. |
| `Login failed for user` | Kiểm tra biến `DB_USERNAME`, `DB_PASSWORD`; dùng script Windows Authentication nếu phù hợp. |
| Thiếu DLL authentication | Chạy `Start-Backend.ps1` thay vì chạy trực tiếp Java; script tự tải đúng DLL. |
| Cổng 8080 hoặc 5173 đã dùng | Dừng phiên chạy cũ của dự án; hoặc đổi cổng BE và sửa proxy FE tương ứng. |
| FE báo không kết nối máy chủ | Chờ BE báo `Started FuNewsApplication`, kiểm tra API công khai ở cổng 8080. |
| Phiên hết hạn / BE vừa khởi động lại | Đăng nhập lại. |
| IntelliJ báo thiếu getter/setter | Reload Maven, bật annotation processing và hỗ trợ Lombok. |

Tham khảo triển khai: [Spring Boot 3](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Vite](https://vite.dev/guide/), [Microsoft JDBC](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17).
