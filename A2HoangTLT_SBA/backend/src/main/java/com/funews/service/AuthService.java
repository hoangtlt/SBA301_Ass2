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
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    public AuthService(AccountRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }

    public Responses.Session login(Requests.Login form, HttpServletRequest request) {
        SystemAccount account = accounts.findByAccountEmailIgnoreCase(form.email().trim()).orElse(null);
        if (account == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        String password = form.password();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        if (!encoder.matches(password, account.getAccountPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        // Tạo phiên đăng nhập mới sau khi xác thực thành công.
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("accountId", account.getAccountId());
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        return new Responses.Session(Responses.Account.from(account), token);
    }

    public SystemAccount current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        Object accountId = session.getAttribute("accountId");
        if (!(accountId instanceof Integer)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        Integer id = (Integer) accountId;
        // Đọc lại tài khoản để áp dụng ngay việc xóa tài khoản hoặc đổi vai trò.
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            session.invalidate();
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tài khoản không còn tồn tại.");
        }
        if (!"GET".equals(request.getMethod())) {
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getHeader("X-CSRF-Token");
            if (sessionToken == null || requestToken == null) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Phiên yêu cầu không hợp lệ. Vui lòng đăng nhập lại.");
            }
            byte[] sessionTokenBytes = sessionToken.getBytes(StandardCharsets.UTF_8);
            byte[] requestTokenBytes = requestToken.getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(sessionTokenBytes, requestTokenBytes)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Phiên yêu cầu không hợp lệ. Vui lòng đăng nhập lại.");
            }
        }
        return account;
    }

    public SystemAccount requireRole(HttpServletRequest request, int role) {
        SystemAccount account = current(request);
        if (account.getAccountRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện chức năng này.");
        }
        return account;
    }

    public Responses.Session me(HttpServletRequest request) {
        SystemAccount account = current(request);
        HttpSession session = request.getSession(false);
        String token = (String) session.getAttribute("csrfToken");
        Responses.Account accountResponse = Responses.Account.from(account);
        return new Responses.Session(accountResponse, token);
    }

    public void logout(HttpServletRequest request) {
        current(request);
        HttpSession session = request.getSession(false);
        session.invalidate();
    }
}
