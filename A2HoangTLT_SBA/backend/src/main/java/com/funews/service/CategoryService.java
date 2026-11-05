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
        Category category = categories.findById(id).orElse(null);
        if (category == null) {
            throw ApiException.notFound("Danh mục");
        }
        return Responses.Category.from(category);
    }

    public Responses.Category create(Requests.Category form) {
        Category parent = null;
        if (form.parentCategoryId() != null) {
            parent = categories.findById(form.parentCategoryId()).orElse(null);
            if (parent == null) {
                throw ApiException.notFound("Danh mục");
            }
        }

        // Duyệt ngược chuỗi cha; HashSet ghi nhớ ID để phát hiện vòng lặp có sẵn.
        Set<Integer> visited = new HashSet<>();
        Category currentParent = parent;
        while (currentParent != null) {
            Integer parentId = currentParent.getCategoryId();
            if (visited.contains(parentId)) {
                throw ApiException.badRequest("Danh mục cha tạo thành vòng lặp.");
            }
            visited.add(parentId);
            currentParent = currentParent.getParentCategory();
        }

        Category category = new Category();
        category.setCategoryName(form.categoryName().trim());
        category.setCategoryDescription(form.categoryDescription().trim());
        category.setParentCategory(parent);
        category.setIsActive(form.isActive());

        Category savedCategory = categories.save(category);
        return Responses.Category.from(savedCategory);
    }

    public Responses.Category update(Integer id, Requests.Category form) {
        Category category = categories.findById(id).orElse(null);
        if (category == null) {
            throw ApiException.notFound("Danh mục");
        }

        Category parent = null;
        if (form.parentCategoryId() != null) {
            parent = categories.findById(form.parentCategoryId()).orElse(null);
            if (parent == null) {
                throw ApiException.notFound("Danh mục");
            }
        }

        // Duyệt tổ tiên của cha mới; gặp chính danh mục đang sửa hoặc ID lặp thì từ chối.
        Set<Integer> visited = new HashSet<>();
        Category currentParent = parent;
        while (currentParent != null) {
            Integer parentId = currentParent.getCategoryId();
            if (parentId.equals(category.getCategoryId()) || visited.contains(parentId)) {
                throw ApiException.badRequest("Danh mục cha tạo thành vòng lặp.");
            }
            visited.add(parentId);
            currentParent = currentParent.getParentCategory();
        }

        category.setCategoryName(form.categoryName().trim());
        category.setCategoryDescription(form.categoryDescription().trim());
        category.setParentCategory(parent);
        category.setIsActive(form.isActive());

        Category savedCategory = categories.save(category);
        return Responses.Category.from(savedCategory);
    }

    public void delete(Integer id) {
        Category category = categories.findById(id).orElse(null);
        if (category == null) {
            throw ApiException.notFound("Danh mục");
        }

        // Chỉ xóa danh mục không còn bài viết hoặc danh mục con tham chiếu đến.
        if (news.existsByCategoryCategoryId(id)) {
            throw ApiException.conflict("Không thể xóa danh mục đang có bài viết.");
        }
        if (categories.existsByParentCategoryCategoryId(id)) {
            throw ApiException.conflict("Không thể xóa danh mục đang có danh mục con.");
        }
        categories.delete(category);
    }

}
