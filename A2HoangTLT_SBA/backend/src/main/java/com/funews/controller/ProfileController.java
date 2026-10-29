/**
 * @author HoangTLT
 */

package com.funews.controller;

import com.funews.dto.*;
import com.funews.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/profile")
public class ProfileController {
    private final AccountService accounts;
    private final AuthService auth;
    public ProfileController(AccountService accounts, AuthService auth) { this.accounts = accounts; this.auth = auth; }
    @GetMapping
    public Responses.Account get(HttpServletRequest request) { return Responses.Account.from(auth.requireRole(request, 2)); }
    @PutMapping
    public Responses.Account update(@Valid @RequestBody Requests.Profile form, HttpServletRequest request) {
        var staff = auth.requireRole(request, 2); return accounts.updateProfile(staff.getAccountId(), form);
    }
}
