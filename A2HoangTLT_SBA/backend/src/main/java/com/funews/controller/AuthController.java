/**
 * @author HoangTLT
 */

package com.funews.controller;

import com.funews.dto.*;
import com.funews.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/login")
    public Responses.Session login(@Valid @RequestBody Requests.Login form, HttpServletRequest request) { return auth.login(form, request); }
    @GetMapping("/me")
    public Responses.Session me(HttpServletRequest request) { return auth.me(request); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) { auth.logout(request); }
}
