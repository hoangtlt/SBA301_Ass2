/**
 * @author HoangTLT
 */

package com.funews.controller;

import com.funews.dto.*;
import com.funews.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accounts;
    private final AuthService auth;

    public AccountController(AccountService accounts, AuthService auth) {
        this.accounts = accounts;
        this.auth = auth;
    }

    @GetMapping
    public List<Responses.Account> search(@RequestParam(defaultValue = "") String q, HttpServletRequest request) {
        auth.requireRole(request, 1);
        return accounts.search(q);
    }

    @GetMapping("/{id}")
    public Responses.Account get(@PathVariable Integer id, HttpServletRequest request) {
        auth.requireRole(request, 1);
        return accounts.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.Account create(@Valid @RequestBody Requests.Account form, HttpServletRequest request) {
        auth.requireRole(request, 1);
        return accounts.create(form);
    }

    @PutMapping("/{id}")
    public Responses.Account update(@PathVariable Integer id, @Valid @RequestBody Requests.Account form,
            HttpServletRequest request) {
        var admin = auth.requireRole(request, 1);
        return accounts.update(id, form, admin.getAccountId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id, HttpServletRequest request) {
        var admin = auth.requireRole(request, 1);
        accounts.delete(id, admin.getAccountId());
    }
}
