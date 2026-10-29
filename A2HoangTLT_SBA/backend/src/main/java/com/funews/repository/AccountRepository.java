/**
 * @author HoangTLT
 */

package com.funews.repository;

import com.funews.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<SystemAccount, Integer> {
    Optional<SystemAccount> findByAccountEmailIgnoreCase(String email);
    List<SystemAccount> findByAccountNameContainingIgnoreCaseOrAccountEmailContainingIgnoreCaseOrderByAccountIdDesc(String name, String email);
    long countByAccountRole(Integer role);
}
