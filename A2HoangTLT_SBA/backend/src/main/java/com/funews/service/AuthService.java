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
import java.util.Optional;
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
        Optional<SystemAccount> accountResult = accounts.findByAccountEmailIgnoreCase(form.email().trim());
        if (accountResult.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
        SystemAccount account = accountResult.get();
        if (form.password().getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(form.password(), account.getAccountPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng.");
        }
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
        // Reload the account so deleted accounts and role changes take effect immediately.
        Optional<SystemAccount> accountResult = accounts.findById(id);
        if (accountResult.isEmpty()) {
            session.invalidate();
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tài khoản không còn tồn tại.");
        }
        SystemAccount account = accountResult.get();
        if (!"GET".equals(request.getMethod())) {
            String expected = (String) session.getAttribute("csrfToken");
            String actual = request.getHeader("X-CSRF-Token");
            if (expected == null || actual == null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
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
        return new Responses.Session(Responses.Account.from(account), (String) request.getSession(false).getAttribute("csrfToken"));
    }

    public void logout(HttpServletRequest request) {
        current(request);
        request.getSession(false).invalidate();
    }
}
