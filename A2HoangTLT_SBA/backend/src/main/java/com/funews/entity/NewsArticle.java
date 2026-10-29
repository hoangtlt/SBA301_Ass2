/**
 * @author HoangTLT
 */

package com.funews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity @Table(name = "NewsArticle")
@Getter @Setter @NoArgsConstructor
public class NewsArticle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NewsArticleID")
    private Integer newsArticleId;
    @Column(name = "NewsTitle", nullable = false, columnDefinition = "nvarchar(200)")
    private String newsTitle;
    @Column(name = "Headline", nullable = false, columnDefinition = "nvarchar(300)")
    private String headline;
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    @Column(name = "NewsContent", nullable = false, columnDefinition = "nvarchar(max)")
    private String newsContent;
    @Column(name = "NewsSource", columnDefinition = "nvarchar(300)")
    private String newsSource;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "CategoryID")
    private Category category;
    @Column(name = "NewsStatus", nullable = false)
    private Integer newsStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "CreatedByID")
    private SystemAccount createdBy;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "UpdatedByID")
    private SystemAccount updatedBy;
    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;
    // The PDF limits a tag to at most one article, so TagID is unique in NewsTag.
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "NewsTag", joinColumns = @JoinColumn(name = "NewsArticleID"),
            inverseJoinColumns = @JoinColumn(name = "TagID"),
            uniqueConstraints = @UniqueConstraint(columnNames = "TagID"))
    private Set<Tag> tags = new LinkedHashSet<>();
}
