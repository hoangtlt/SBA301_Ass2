/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.*;
import com.funews.entity.*;
import com.funews.exception.ApiException;
import com.funews.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional
public class NewsService {
    private final NewsRepository news;
    private final CategoryRepository categories;
    private final TagRepository tags;
    public NewsService(NewsRepository news, CategoryRepository categories, TagRepository tags) {
        this.news = news; this.categories = categories; this.tags = tags;
    }
    @Transactional(readOnly = true)
    public List<Responses.News> search(String q, Integer status, Integer authorId) {
        return news.search(q.trim(), status, authorId).stream().map(Responses.News::from).toList();
    }
    @Transactional(readOnly = true)
    public Responses.News get(Integer id, boolean publicOnly) {
        var article = find(id);
        if (publicOnly && article.getNewsStatus() != 1) throw ApiException.notFound("Bài viết");
        return Responses.News.from(article);
    }
    private NewsArticle find(Integer id) { return news.findById(id).orElseThrow(() -> ApiException.notFound("Bài viết")); }
}
