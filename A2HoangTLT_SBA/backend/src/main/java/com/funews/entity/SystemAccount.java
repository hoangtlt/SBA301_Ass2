/**
 * @author HoangTLT
 */

package com.funews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SystemAccount", uniqueConstraints = @UniqueConstraint(columnNames = "AccountEmail"))
@Getter @Setter @NoArgsConstructor
public class SystemAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountId;
    @Column(name = "AccountName", nullable = false, length = 100, columnDefinition = "nvarchar(100)")
    private String accountName;
    @Column(name = "AccountEmail", nullable = false, length = 255)
    private String accountEmail;
    @Column(name = "AccountRole", nullable = false)
    private Integer accountRole;
    @Column(name = "AccountPassword", nullable = false, length = 100)
    private String accountPassword;
}
