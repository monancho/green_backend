package com.green.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.green.university.repository.model.Staff;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Staff} entities.
 */
public interface
StaffJpaRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByIdAndNameAndEmail(Integer id, String name, String email);
    Optional<Staff> findByNameAndEmail(String name, String email);
    List<Staff> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

}