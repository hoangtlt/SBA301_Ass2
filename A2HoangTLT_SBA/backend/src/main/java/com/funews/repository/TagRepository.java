/**
 * @author HoangTLT
 */

package com.funews.repository;

import com.funews.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> { }
