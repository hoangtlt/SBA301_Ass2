/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.Requests;
import com.funews.dto.Responses;
import com.funews.entity.NewsArticle;
import com.funews.entity.SystemAccount;
import com.funews.exception.ApiException;
import com.funews.repository.AccountRepository;
import com.funews.repository.NewsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accounts;
    private final NewsRepository news;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, NewsRepository news, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.news = news;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Responses.Account> search(String q) {
        List<SystemAccount> accountList = accounts.findByAccountNameContainingIgnoreCaseOrAccountEmailContainingIgnoreCaseOrderByAccountIdDesc(q.trim(), q.trim());
        List<Responses.Account> result = new ArrayList<>();
        for (SystemAccount account : accountList) {
            result.add(Responses.Account.from(account));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Responses.Account get(Integer id) {
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            throw ApiException.notFound("Tài khoản");
        }
        return Responses.Account.from(account);
    }

    public Responses.Account create(Requests.Account form) {
        SystemAccount existingAccount = accounts.findByAccountEmailIgnoreCase(form.accountEmail().trim()).orElse(null);
        if (existingAccount != null) {
            throw ApiException.conflict("Email đã được sử dụng.");
        }

        SystemAccount account = new SystemAccount();
        account.setAccountName(form.accountName().trim());
        account.setAccountEmail(form.accountEmail().trim().toLowerCase(Locale.ROOT));
        account.setAccountRole(form.accountRole());

        String password = form.password();
        if (password == null || password.isBlank() || password.length() < 6
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.badRequest("Mật khẩu phải có ít nhất 6 ký tự và tối đa 72 byte UTF-8.");
        }
        account.setAccountPassword(encoder.encode(password));

        SystemAccount savedAccount = accounts.save(account);
        return Responses.Account.from(savedAccount);
    }

    public Responses.Account update(Integer id, Requests.Account form, Integer currentId) {
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            throw ApiException.notFound("Tài khoản");
        }

        if (id.equals(currentId) && form.accountRole() != 1) {
            throw ApiException.badRequest("Không thể tự đổi vai trò của tài khoản đang đăng nhập.");
        }
        if (account.getAccountRole() == 1 && form.accountRole() != 1 && accounts.countByAccountRole(1) <= 1) {
            throw ApiException.badRequest("Hệ thống phải có ít nhất một Admin.");
        }
        if (account.getAccountRole() == 2 && form.accountRole() != 2 && news.existsByCreatedByAccountId(id)) {
            throw ApiException.conflict("Không thể đổi vai trò Staff đã tạo bài viết.");
        }
        SystemAccount existingAccount = accounts.findByAccountEmailIgnoreCase(form.accountEmail().trim()).orElse(null);
        if (existingAccount != null && !existingAccount.getAccountId().equals(account.getAccountId())) {
            throw ApiException.conflict("Email đã được sử dụng.");
        }
        account.setAccountName(form.accountName().trim());
        account.setAccountEmail(form.accountEmail().trim().toLowerCase(Locale.ROOT));
        account.setAccountRole(form.accountRole());

        String password = form.password();
        if (password != null && !password.isEmpty()) {
            if (password.isBlank() || password.length() < 6
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw ApiException.badRequest("Mật khẩu phải có ít nhất 6 ký tự và tối đa 72 byte UTF-8.");
            }
            account.setAccountPassword(encoder.encode(password));
        }

        SystemAccount savedAccount = accounts.save(account);
        return Responses.Account.from(savedAccount);
    }

    public void delete(Integer id, Integer currentId) {
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            throw ApiException.notFound("Tài khoản");
        }

        if (news.existsByCreatedByAccountId(id)) {
            throw ApiException.conflict("Không thể xóa tài khoản đã tạo bài viết.");
        }
        if (id.equals(currentId)) {
            throw ApiException.badRequest("Không thể xóa tài khoản đang đăng nhập.");
        }
        if (account.getAccountRole() == 1 && accounts.countByAccountRole(1) <= 1) {
            throw ApiException.badRequest("Hệ thống phải có ít nhất một Admin.");
        }
        if (news.existsByUpdatedByAccountId(id)) {
            // Giữ lại bài viết và bỏ thông tin người chỉnh sửa khi xóa tài khoản.
            for (NewsArticle article : news.findAll()) {
                if (article.getUpdatedBy() != null && article.getUpdatedBy().getAccountId().equals(id)) {
                    article.setUpdatedBy(null);
                }
            }
            news.flush();
        }
        accounts.delete(account);
    }

    public Responses.Account updateProfile(Integer id, Requests.Profile form) {
        SystemAccount account = accounts.findById(id).orElse(null);
        if (account == null) {
            throw ApiException.notFound("Tài khoản");
        }

        if (form.currentPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(form.currentPassword(), account.getAccountPassword())) {
            throw ApiException.badRequest("Mật khẩu hiện tại không đúng.");
        }
        SystemAccount existingAccount = accounts.findByAccountEmailIgnoreCase(form.accountEmail().trim()).orElse(null);
        if (existingAccount != null && !existingAccount.getAccountId().equals(id)) {
            throw ApiException.conflict("Email đã được sử dụng.");
        }
        account.setAccountName(form.accountName().trim());
        account.setAccountEmail(form.accountEmail().trim().toLowerCase(Locale.ROOT));

        String newPassword = form.newPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.isBlank() || newPassword.length() < 6
                    || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw ApiException.badRequest("Mật khẩu phải có ít nhất 6 ký tự và tối đa 72 byte UTF-8.");
            }
            account.setAccountPassword(encoder.encode(newPassword));
        }

        SystemAccount savedAccount = accounts.save(account);
        return Responses.Account.from(savedAccount);
    }
}
