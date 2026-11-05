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
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categories;
    private final AuthService auth;

    public CategoryController(CategoryService categories, AuthService auth) {
        this.categories = categories;
        this.auth = auth;
    }

    @GetMapping
    public List<Responses.Category> search(@RequestParam(defaultValue = "") String q, HttpServletRequest request) {
        auth.requireRole(request, 2);
        return categories.search(q);
    }

    @GetMapping("/{id}")
    public Responses.Category get(@PathVariable Integer id, HttpServletRequest request) {
        auth.requireRole(request, 2);
        return categories.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.Category create(@Valid @RequestBody Requests.Category form, HttpServletRequest request) {
        auth.requireRole(request, 2);
        return categories.create(form);
    }

    @PutMapping("/{id}")
    public Responses.Category update(@PathVariable Integer id, @Valid @RequestBody Requests.Category form,
            HttpServletRequest request) {
        auth.requireRole(request, 2);
        return categories.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id, HttpServletRequest request) {
        auth.requireRole(request, 2);
        categories.delete(id);
    }
}
