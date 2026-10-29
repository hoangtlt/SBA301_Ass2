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
    public Responses.News create(Requests.News form, SystemAccount author) {
        var article = new NewsArticle();
        article.setCreatedBy(author);
        article.setCreatedDate(LocalDateTime.now());
        apply(article, form);
        return Responses.News.from(news.save(article));
    }
    public Responses.News update(Integer id, Requests.News form, SystemAccount editor) {
        var article = find(id);
        var oldTags = new ArrayList<>(article.getTags());
        apply(article, form);
        article.setUpdatedBy(editor);
        article.setModifiedDate(LocalDateTime.now());
        news.saveAndFlush(article);
        tags.deleteAll(oldTags);
        return Responses.News.from(article);
    }
    public void delete(Integer id) {
        var article = find(id);
        var oldTags = new ArrayList<>(article.getTags());
        news.delete(article);
        news.flush();
        tags.deleteAll(oldTags);
    }
    private void apply(NewsArticle article, Requests.News form) {
        var category = categories.findById(form.categoryId()).orElseThrow(() -> ApiException.notFound("Danh mục"));
        // Allow editing a draft in its existing inactive category, but never publish into it.
        boolean sameCategory = article.getCategory() != null && article.getCategory().getCategoryId().equals(category.getCategoryId());
        if (!category.getIsActive() && (form.newsStatus() == 1 || !sameCategory)) {
            throw ApiException.badRequest("Vui lòng chọn danh mục đang hoạt động.");
        }
        var names = new HashSet<String>();
        var newTags = new ArrayList<Tag>();
        for (var tagForm : form.tags()) {
            String name = tagForm.tagName().trim();
            if (!names.add(name.toLowerCase(Locale.ROOT))) throw ApiException.badRequest("Tag trong cùng bài viết không được trùng tên.");
            var tag = new Tag();
            tag.setTagName(name);
            tag.setNote(tagForm.note() == null ? "" : tagForm.note().trim());
            newTags.add(tag);
        }
        article.setNewsTitle(form.newsTitle().trim());
        article.setHeadline(form.headline().trim());
        article.setNewsContent(form.newsContent().trim());
        article.setNewsSource(form.newsSource() == null ? "" : form.newsSource().trim());
        article.setCategory(category);
        article.setNewsStatus(form.newsStatus());
        article.getTags().clear();
        article.getTags().addAll(newTags);
    }
    private NewsArticle find(Integer id) { return news.findById(id).orElseThrow(() -> ApiException.notFound("Bài viết")); }
}
