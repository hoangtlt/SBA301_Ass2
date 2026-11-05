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
import java.util.Set;

// Bài viết, liên kết và tags cùng transaction để tránh lưu dở dang khi có lỗi.
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
        NewsArticle article = news.findById(id).orElse(null);
        if (article == null) {
            throw ApiException.notFound("Bài viết");
        }

        // Trả 404 cho bản nháp để không lộ bài qua URL công khai chứa ID.
        if (publicOnly && article.getNewsStatus() != 1) {
            throw ApiException.notFound("Bài viết");
        }
        return Responses.News.from(article);
    }

    public Responses.News create(Requests.News form, SystemAccount author) {
        NewsArticle article = new NewsArticle();
        article.setCreatedBy(author);
        article.setCreatedDate(LocalDateTime.now());
        Category category = categories.findById(form.categoryId()).orElse(null);
        if (category == null) {
            throw ApiException.notFound("Danh mục");
        }
        if (!category.getIsActive()) {
            throw ApiException.badRequest("Vui lòng chọn danh mục đang hoạt động.");
        }

        // Chuẩn hóa tên rồi dùng HashSet để chặn tag trùng, không phân biệt hoa/thường.
        Set<String> tagNames = new HashSet<>();
        List<Tag> newTags = new ArrayList<>();
        for (Requests.Tag tagForm : form.tags()) {
            String name = tagForm.tagName().trim();
            String normalizedName = name.toLowerCase(Locale.ROOT);
            if (tagNames.contains(normalizedName)) {
                throw ApiException.badRequest("Tag trong cùng bài viết không được trùng tên.");
            }
            tagNames.add(normalizedName);

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
        article.getTags().addAll(newTags);

        NewsArticle savedArticle = news.save(article);
        return Responses.News.from(savedArticle);
    }

    public Responses.News update(Integer id, Requests.News form, SystemAccount editor) {
        NewsArticle article = news.findById(id).orElse(null);
        if (article == null) {
            throw ApiException.notFound("Bài viết");
        }
        List<Tag> oldTags = new ArrayList<>(article.getTags());

        Category category = categories.findById(form.categoryId()).orElse(null);
        if (category == null) {
            throw ApiException.notFound("Danh mục");
        }
        // Danh mục ngừng hoạt động chỉ được giữ lại khi lưu bản nháp trong danh mục cũ.
        boolean sameCategory = false;
        if (article.getCategory() != null) {
            sameCategory = article.getCategory().getCategoryId().equals(category.getCategoryId());
        }
        if (!category.getIsActive()) {
            if (form.newsStatus() == 1 || !sameCategory) {
                throw ApiException.badRequest("Vui lòng chọn danh mục đang hoạt động.");
            }
        }

        // Chuẩn hóa tên rồi dùng HashSet để chặn tag trùng trong danh sách thay thế.
        Set<String> tagNames = new HashSet<>();
        List<Tag> newTags = new ArrayList<>();
        for (Requests.Tag tagForm : form.tags()) {
            String name = tagForm.tagName().trim();
            String normalizedName = name.toLowerCase(Locale.ROOT);
            if (tagNames.contains(normalizedName)) {
                throw ApiException.badRequest("Tag trong cùng bài viết không được trùng tên.");
            }
            tagNames.add(normalizedName);

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
        // Thay toàn bộ tags; danh sách oldTags được giữ riêng để dọn sau khi gỡ liên kết.
        article.getTags().clear();
        article.getTags().addAll(newTags);
        article.setUpdatedBy(editor);
        article.setModifiedDate(LocalDateTime.now());

        // Flush thay đổi bảng NewsTag trước khi xóa tag cũ để tránh lỗi khóa ngoại.
        news.saveAndFlush(article);
        tags.deleteAll(oldTags);
        return Responses.News.from(article);
    }

    public void delete(Integer id) {
        NewsArticle article = news.findById(id).orElse(null);
        if (article == null) {
            throw ApiException.notFound("Bài viết");
        }
        List<Tag> oldTags = new ArrayList<>(article.getTags());
        // Xóa bài và liên kết trước; tags thuộc riêng bài nên được dọn sau khi flush.
        news.delete(article);
        news.flush();
        tags.deleteAll(oldTags);
    }

}
