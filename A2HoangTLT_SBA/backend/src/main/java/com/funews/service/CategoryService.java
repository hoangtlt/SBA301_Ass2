/**
 * @author HoangTLT
 */

package com.funews.service;

import com.funews.dto.Requests;
import com.funews.dto.Responses;
import com.funews.entity.Category;
import com.funews.exception.ApiException;
import com.funews.repository.CategoryRepository;
import com.funews.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categories;
    private final NewsRepository news;

    public CategoryService(CategoryRepository categories, NewsRepository news) {
        this.categories = categories;
        this.news = news;
    }

    @Transactional(readOnly = true)
    public List<Responses.Category> search(String q) {
        List<Category> categoryList = categories.findByCategoryNameContainingIgnoreCaseOrCategoryDescriptionContainingIgnoreCaseOrderByCategoryIdDesc(q.trim(), q.trim());
        List<Responses.Category> result = new ArrayList<>();
        for (Category category : categoryList) {
            result.add(Responses.Category.from(category));
        }
        return Collections.unmodifiableList(result);
    }

    @Transactional(readOnly = true)
    public Responses.Category get(Integer id) {
        return Responses.Category.from(find(id));
    }

    public Responses.Category create(Requests.Category form) {
        return save(new Category(), form);
    }

    public Responses.Category update(Integer id, Requests.Category form) {
        return save(find(id), form);
    }

    public void delete(Integer id) {
        Category category = find(id);
        if (news.existsByCategoryCategoryId(id)) {
            throw ApiException.conflict("Không thể xóa danh mục đang có bài viết.");
        }
        if (categories.existsByParentCategoryCategoryId(id)) {
            throw ApiException.conflict("Không thể xóa danh mục đang có danh mục con.");
        }
        categories.delete(category);
    }

    private Responses.Category save(Category category, Requests.Category form) {
        Category parent = null;
        if (form.parentCategoryId() != null) {
            parent = find(form.parentCategoryId());
        }
        Set<Integer> visited = new HashSet<>();
        for (Category cursor = parent; cursor != null; cursor = cursor.getParentCategory()) {
            if (cursor.getCategoryId().equals(category.getCategoryId()) || !visited.add(cursor.getCategoryId())) {
                throw ApiException.badRequest("Danh mục cha tạo thành vòng lặp.");
            }
        }
        category.setCategoryName(form.categoryName().trim());
        category.setCategoryDescription(form.categoryDescription().trim());
        category.setParentCategory(parent);
        category.setIsActive(form.isActive());
        return Responses.Category.from(categories.save(category));
    }

    private Category find(Integer id) {
        Optional<Category> category = categories.findById(id);
        if (category.isEmpty()) {
            throw ApiException.notFound("Danh mục");
        }
        return category.get();
    }
}
