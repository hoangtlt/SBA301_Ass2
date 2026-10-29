/**
 * @author HoangTLT
 */

package com.funews.controller;

import com.funews.dto.Responses;
import com.funews.service.NewsService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/public/news")
public class PublicNewsController {
    private final NewsService news;
    public PublicNewsController(NewsService news) { this.news = news; }
    @GetMapping
    public List<Responses.News> search(@RequestParam(defaultValue = "") String q) { return news.search(q, 1, null); }
    @GetMapping("/{id}")
    public Responses.News get(@PathVariable Integer id) { return news.get(id, true); }
}
