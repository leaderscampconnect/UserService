package com.campconnect.userservice.repository;

import com.campconnect.userservice.entity.User;
import com.campconnect.userservice.entity.User.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
}