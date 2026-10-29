/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.*;
import com.funews.entity.SystemAccount;
import com.funews.exception.ApiException;
import com.funews.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service @Transactional
public class AccountService {
    private final AccountRepository accounts;
    private final NewsRepository news;
    private final PasswordEncoder encoder;
    public AccountService(AccountRepository accounts, NewsRepository news, PasswordEncoder encoder) {
        this.accounts = accounts; this.news = news; this.encoder = encoder;
    }
    @Transactional(readOnly = true)
    public List<Responses.Account> search(String q) {
        return accounts.findByAccountNameContainingIgnoreCaseOrAccountEmailContainingIgnoreCaseOrderByAccountIdDesc(q.trim(), q.trim())
                .stream().map(Responses.Account::from).toList();
    }
    @Transactional(readOnly = true)
    public Responses.Account get(Integer id) { return Responses.Account.from(find(id)); }
    public Responses.Account create(Requests.Account form) {
        var account = new SystemAccount();
        apply(account, form);
        validatePassword(form.password());
        account.setAccountPassword(encoder.encode(form.password()));
        return Responses.Account.from(accounts.save(account));
    }
    public Responses.Account update(Integer id, Requests.Account form, Integer currentId) {
        var account = find(id);
        if (id.equals(currentId) && form.accountRole() != 1) throw ApiException.badRequest("Không thể tự đổi vai trò của tài khoản đang đăng nhập.");
        if (account.getAccountRole() == 1 && form.accountRole() != 1 && accounts.countByAccountRole(1) <= 1) {
            throw ApiException.badRequest("Hệ thống phải có ít nhất một Admin.");
        }
        if (account.getAccountRole() == 2 && form.accountRole() != 2 && news.existsByCreatedByAccountId(id)) {
            throw ApiException.conflict("Không thể đổi vai trò Staff đã tạo bài viết.");
        }
        apply(account, form);
        if (form.password() != null && !form.password().isEmpty()) {
            validatePassword(form.password());
            account.setAccountPassword(encoder.encode(form.password()));
        }
        return Responses.Account.from(accounts.save(account));
    }
    public void delete(Integer id, Integer currentId) {
        var account = find(id);
        if (news.existsByCreatedByAccountId(id)) throw ApiException.conflict("Không thể xóa tài khoản đã tạo bài viết.");
        if (id.equals(currentId)) throw ApiException.badRequest("Không thể xóa tài khoản đang đăng nhập.");
        if (account.getAccountRole() == 1 && accounts.countByAccountRole(1) <= 1) throw ApiException.badRequest("Hệ thống phải có ít nhất một Admin.");
        if (news.existsByUpdatedByAccountId(id)) {
            // UpdatedBy is optional; preserve articles if only their editor is deleted.
            for (var article : news.findAll()) {
                if (article.getUpdatedBy() != null && article.getUpdatedBy().getAccountId().equals(id)) article.setUpdatedBy(null);
            }
            news.flush();
        }
        accounts.delete(account);
    }
    private SystemAccount find(Integer id) { return accounts.findById(id).orElseThrow(() -> ApiException.notFound("Tài khoản")); }
    private void apply(SystemAccount account, Requests.Account form) {
        checkEmail(form.accountEmail(), account.getAccountId());
        account.setAccountName(form.accountName().trim());
        account.setAccountEmail(form.accountEmail().trim().toLowerCase(Locale.ROOT));
        account.setAccountRole(form.accountRole());
    }
    private void checkEmail(String email, Integer id) {
        accounts.findByAccountEmailIgnoreCase(email.trim()).ifPresent(existing -> {
            if (!existing.getAccountId().equals(id)) throw ApiException.conflict("Email đã được sử dụng.");
        });
    }
    public static void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 6 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.badRequest("Mật khẩu phải có ít nhất 6 ký tự và tối đa 72 byte UTF-8.");
        }
    }
}
