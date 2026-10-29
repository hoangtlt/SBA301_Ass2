/**
 * @author HoangTLT
 */

package com.funews.dto;

import com.funews.entity.*;
import java.time.LocalDateTime;
import java.util.List;

// Responses never expose password hashes or JPA relationships directly.
public class Responses {
    public record Account(Integer accountId, String accountName, String accountEmail, Integer accountRole) {
        public static Account from(SystemAccount a) {
            return new Account(a.getAccountId(), a.getAccountName(), a.getAccountEmail(), a.getAccountRole());
        }
    }
    public record Session(Account account, String csrfToken) { }
    public record Category(Integer categoryId, String categoryName, String categoryDescription,
                           Integer parentCategoryId, String parentCategoryName, Boolean isActive) {
        public static Category from(com.funews.entity.Category c) {
            var parent = c.getParentCategory();
            return new Category(c.getCategoryId(), c.getCategoryName(), c.getCategoryDescription(),
                    parent == null ? null : parent.getCategoryId(), parent == null ? null : parent.getCategoryName(), c.getIsActive());
        }
    }
    public record Tag(Integer tagId, String tagName, String note) {
        public static Tag from(com.funews.entity.Tag t) { return new Tag(t.getTagId(), t.getTagName(), t.getNote()); }
    }
    public record News(Integer newsArticleId, String newsTitle, String headline, String newsContent,
                       String newsSource, Integer categoryId, String categoryName, Integer newsStatus,
                       Integer createdById, String createdByName, Integer updatedById, String updatedByName,
                       LocalDateTime createdDate, LocalDateTime modifiedDate, List<Tag> tags) {
        public static News from(NewsArticle n) {
            var updater = n.getUpdatedBy();
            return new News(n.getNewsArticleId(), n.getNewsTitle(), n.getHeadline(), n.getNewsContent(),
                    n.getNewsSource(), n.getCategory().getCategoryId(), n.getCategory().getCategoryName(), n.getNewsStatus(),
                    n.getCreatedBy().getAccountId(), n.getCreatedBy().getAccountName(),
                    updater == null ? null : updater.getAccountId(), updater == null ? null : updater.getAccountName(),
                    n.getCreatedDate(), n.getModifiedDate(), n.getTags().stream().map(Tag::from).toList());
        }
    }
}
