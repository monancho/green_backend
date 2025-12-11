package com.green.university.repository;

import com.green.university.repository.model.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.green.university.repository.model.College;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * JPA repository for {@link College} entities.
 */
public interface CollegeJpaRepository extends JpaRepository<College, Integer> {


    // 해당 이름을 가진 단과대가 몇 개 있는지 개수를 반환
    public int countByName(String name);

    List<College> findAllByOrderByIdAsc();

}
