/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.Requests;
import com.funews.dto.Responses;
import com.funews.entity.Category;
import com.funews.entity.NewsArticle;
import com.funews.entity.SystemAccount;
import com.funews.entity.Tag;
import com.funews.exception.ApiException;
import com.funews.repository.CategoryRepository;
import com.funews.repository.NewsRepository;
import com.funews.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class NewsService {
    private final NewsRepository news;
    private final CategoryRepository categories;
    private final TagRepository tags;

    public NewsService(NewsRepository news, CategoryRepository categories, TagRepository tags) {
        this.news = news;
        this.categories = categories;
        this.tags = tags;
    }

    @Transactional(readOnly = true)
    public List<Responses.News> search(String q, Integer status, Integer authorId) {
        List<NewsArticle> articles = news.search(q.trim(), status, authorId);
        List<Responses.News> result = new ArrayList<>();
        for (NewsArticle article : articles) {
            result.add(Responses.News.from(article));
        }
        return Collections.unmodifiableList(result);
    }

    @Transactional(readOnly = true)
    public Responses.News get(Integer id, boolean publicOnly) {
        NewsArticle article = find(id);
        if (publicOnly && article.getNewsStatus() != 1) {
            throw ApiException.notFound("Bài viết");
        }
        return Responses.News.from(article);
    }

    public Responses.News create(Requests.News form, SystemAccount author) {
        NewsArticle article = new NewsArticle();
        article.setCreatedBy(author);
        article.setCreatedDate(LocalDateTime.now());
        apply(article, form);
        return Responses.News.from(news.save(article));
    }

    public Responses.News update(Integer id, Requests.News form, SystemAccount editor) {
        NewsArticle article = find(id);
        List<Tag> oldTags = new ArrayList<>(article.getTags());
        apply(article, form);
        article.setUpdatedBy(editor);
        article.setModifiedDate(LocalDateTime.now());
        news.saveAndFlush(article);
        tags.deleteAll(oldTags);
        return Responses.News.from(article);
    }

    public void delete(Integer id) {
        NewsArticle article = find(id);
        List<Tag> oldTags = new ArrayList<>(article.getTags());
        news.delete(article);
        news.flush();
        tags.deleteAll(oldTags);
    }

    private void apply(NewsArticle article, Requests.News form) {
        Optional<Category> categoryResult = categories.findById(form.categoryId());
        if (categoryResult.isEmpty()) {
            throw ApiException.notFound("Danh mục");
        }
        Category category = categoryResult.get();
        // Allow editing a draft in its existing inactive category, but never publish into it.
        boolean sameCategory = article.getCategory() != null && article.getCategory().getCategoryId().equals(category.getCategoryId());
        if (!category.getIsActive() && (form.newsStatus() == 1 || !sameCategory)) {
            throw ApiException.badRequest("Vui lòng chọn danh mục đang hoạt động.");
        }
        Set<String> names = new HashSet<>();
        List<Tag> newTags = new ArrayList<>();
        for (Requests.Tag tagForm : form.tags()) {
            String name = tagForm.tagName().trim();
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw ApiException.badRequest("Tag trong cùng bài viết không được trùng tên.");
            }
            Tag tag = new Tag();
            tag.setTagName(name);
            if (tagForm.note() == null) {
                tag.setNote("");
            } else {
                tag.setNote(tagForm.note().trim());
            }
            newTags.add(tag);
        }
        article.setNewsTitle(form.newsTitle().trim());
        article.setHeadline(form.headline().trim());
        article.setNewsContent(form.newsContent().trim());
        if (form.newsSource() == null) {
            article.setNewsSource("");
        } else {
            article.setNewsSource(form.newsSource().trim());
        }
        article.setCategory(category);
        article.setNewsStatus(form.newsStatus());
        article.getTags().clear();
        article.getTags().addAll(newTags);
    }

    private NewsArticle find(Integer id) {
        Optional<NewsArticle> article = news.findById(id);
        if (article.isEmpty()) {
            throw ApiException.notFound("Bài viết");
        }
        return article.get();
    }
}
