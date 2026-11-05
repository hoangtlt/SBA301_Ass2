/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.Requests;
import com.funews.dto.Responses;
import com.funews.entity.SystemAccount;
import com.funews.exception.ApiException;
import com.funews.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class AuthService {
    // Repository tra cứu tài khoản; encoder kiểm tra mật khẩu với bản băm đã lưu.
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    // Spring truyền các dependency vào service qua constructor.
    public AuthService(AccountRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }

    public Responses.Session login(Requests.Login form, HttpServletRequest request) {
        // Tìm email không phân biệt hoa/thường; dùng lỗi chung để tránh tiết lộ email tồn tại.
        SystemAccount account = accounts.findByAccountEmailIgnoreCase(form.email().trim()).orElse(null);
        if (account == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        // Chặn mật khẩu vượt giới hạn 72 byte UTF-8 của BCrypt trước khi so khớp.
        String password = form.password();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        // So khớp mật khẩu nhập với bản băm; không giải mã mật khẩu trong database.
        if (!encoder.matches(password, account.getAccountPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        // Hủy phiên cũ để tránh tái sử dụng session ID có từ trước khi đăng nhập.
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        // Tạo phiên mới và lưu ID tài khoản để nhận diện người dùng ở các request sau.
        HttpSession session = request.getSession(true);
        session.setAttribute("accountId", account.getAccountId());
        // Lưu CSRF token trong session và gửi cho FE để đính kèm request thay đổi dữ liệu.
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        // Trả thông tin tài khoản qua DTO, không đưa mật khẩu/bản băm ra FE.
        return new Responses.Session(Responses.Account.from(account), token);
    }

    public SystemAccount current(HttpServletRequest request) {
        // Chỉ lấy phiên đang tồn tại; không tự tạo phiên cho người chưa đăng nhập.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        // Session hợp lệ phải chứa ID tài khoản kiểu Integer do login lưu vào.
        Object accountId = session.getAttribute("accountId");
        if (!(accountId instanceof Integer)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        Integer id = (Integer) accountId;
        // Đọc lại tài khoản để áp dụng ngay việc xóa tài khoản hoặc đổi vai trò.
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            // Tài khoản đã bị xóa thì hủy luôn phiên đang giữ ID cũ.
            session.invalidate();
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tài khoản không còn tồn tại.");
        }
        // Yêu cầu token khớp session cho request khác GET để chống giả mạo yêu cầu (CSRF).
        if (!"GET".equals(request.getMethod())) {
            // Đối chiếu token của phiên với token FE gửi trong header; thiếu thì từ chối.
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getHeader("X-CSRF-Token");
            if (sessionToken == null || requestToken == null) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Phiên yêu cầu không hợp lệ. Vui lòng đăng nhập lại.");
            }
            // Chuyển cả hai token sang byte UTF-8 để so sánh cùng một định dạng.
            byte[] sessionTokenBytes = sessionToken.getBytes(StandardCharsets.UTF_8);
            byte[] requestTokenBytes = requestToken.getBytes(StandardCharsets.UTF_8);
            // So sánh token bằng hàm chuyên dụng để hạn chế lộ thông tin qua thời gian so sánh.
            if (!MessageDigest.isEqual(sessionTokenBytes, requestTokenBytes)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Phiên yêu cầu không hợp lệ. Vui lòng đăng nhập lại.");
            }
        }
        // Chỉ trả tài khoản sau khi phiên và kiểm tra CSRF cần thiết đều hợp lệ.
        return account;
    }

    public SystemAccount requireRole(HttpServletRequest request, int role) {
        // Xác thực phiên và CSRF trước khi kiểm tra quyền truy cập chức năng.
        SystemAccount account = current(request);
        // Vai trò phải khớp chính xác: 1 = Admin, 2 = Staff; sai quyền trả 403.
        if (account.getAccountRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện chức năng này.");
        }
        return account;
    }

    public Responses.Session me(HttpServletRequest request) {
        // Xác thực lại để FE khôi phục tài khoản khi tải lại trang.
        SystemAccount account = current(request);
        // Lấy lại token của phiên hiện tại, không cấp phiên hoặc token mới.
        HttpSession session = request.getSession(false);
        String token = (String) session.getAttribute("csrfToken");
        // Đóng gói thông tin tài khoản an toàn và token theo cùng cấu trúc với login.
        Responses.Account accountResponse = Responses.Account.from(account);
        return new Responses.Session(accountResponse, token);
    }

    public void logout(HttpServletRequest request) {
        // Xác thực phiên và CSRF cho request đăng xuất trước khi hủy phiên.
        current(request);
        // Hủy toàn bộ dữ liệu phiên, gồm accountId và csrfToken.
        HttpSession session = request.getSession(false);
        session.invalidate();
    }
}
