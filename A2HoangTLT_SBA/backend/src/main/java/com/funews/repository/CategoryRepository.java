/**
 * @author HoangTLT
 */

package com.funews.repository;

import com.funews.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCategoryNameContainingIgnoreCaseOrCategoryDescriptionContainingIgnoreCaseOrderByCategoryIdDesc(String name, String description);
    boolean existsByParentCategoryCategoryId(Integer id);
}
