/**
 * @author HoangTLT
 */

package com.funews.repository;

import com.funews.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NewsRepository extends JpaRepository<NewsArticle, Integer> {
    @Query("""
        select distinct n from NewsArticle n left join n.tags t
        where (:status is null or n.newsStatus = :status)
        and (:authorId is null or n.createdBy.accountId = :authorId)
        and (lower(n.newsTitle) like lower(concat('%', :q, '%'))
          or lower(n.headline) like lower(concat('%', :q, '%'))
          or lower(n.newsContent) like lower(concat('%', :q, '%'))
          or lower(n.category.categoryName) like lower(concat('%', :q, '%'))
          or lower(t.tagName) like lower(concat('%', :q, '%')))
        order by n.createdDate desc, n.newsArticleId desc
        """)
    List<NewsArticle> search(@Param("q") String query, @Param("status") Integer status, @Param("authorId") Integer authorId);
    boolean existsByCategoryCategoryId(Integer id);
    boolean existsByCreatedByAccountId(Integer id);
    boolean existsByUpdatedByAccountId(Integer id);
}
