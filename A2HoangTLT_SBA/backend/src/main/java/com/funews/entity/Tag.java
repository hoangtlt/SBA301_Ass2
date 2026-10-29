/**
 * @author HoangTLT
 */

package com.funews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "Tag")
@Getter @Setter @NoArgsConstructor
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TagID")
    private Integer tagId;
    @Column(name = "TagName", nullable = false, columnDefinition = "nvarchar(50)")
    private String tagName;
    @Column(name = "Note", columnDefinition = "nvarchar(250)")
    private String note;
}
