package com.studentmanagement.repository;

import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    List<UserAccount> findAllByOrderByCreatedAtDesc();
}
