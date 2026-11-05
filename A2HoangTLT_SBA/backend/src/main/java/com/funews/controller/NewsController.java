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
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService news;
    private final AuthService auth;

    public NewsController(NewsService news, AuthService auth) {
        this.news = news;
        this.auth = auth;
    }

    @GetMapping
    public List<Responses.News> search(@RequestParam(defaultValue = "") String q, HttpServletRequest request) {
        auth.requireRole(request, 2);
        return news.search(q, null, null);
    }

    @GetMapping("/history")
    public List<Responses.News> history(@RequestParam(defaultValue = "") String q, HttpServletRequest request) {
        var staff = auth.requireRole(request, 2);
        return news.search(q, null, staff.getAccountId());
    }

    @GetMapping("/{id}")
    public Responses.News get(@PathVariable Integer id, HttpServletRequest request) {
        auth.requireRole(request, 2);
        return news.get(id, false);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.News create(@Valid @RequestBody Requests.News form, HttpServletRequest request) {
        var staff = auth.requireRole(request, 2);
        return news.create(form, staff);
    }

    @PutMapping("/{id}")
    public Responses.News update(@PathVariable Integer id, @Valid @RequestBody Requests.News form,
            HttpServletRequest request) {
        var staff = auth.requireRole(request, 2);
        return news.update(id, form, staff);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id, HttpServletRequest request) {
        auth.requireRole(request, 2);
        news.delete(id);
    }
}
