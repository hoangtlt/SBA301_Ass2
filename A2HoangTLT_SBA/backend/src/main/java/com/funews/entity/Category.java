/**
 * @author HoangTLT
 */

package com.funews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "Category")
@Getter @Setter @NoArgsConstructor
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer categoryId;
    @Column(name = "CategoryName", nullable = false, columnDefinition = "nvarchar(100)")
    private String categoryName;
    @Column(name = "CategoryDescription", nullable = false, columnDefinition = "nvarchar(500)")
    private String categoryDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentCategoryID")
    private Category parentCategory;
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;
}
