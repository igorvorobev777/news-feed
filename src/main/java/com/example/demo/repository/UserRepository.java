package com.example.demo.repository;

import com.example.demo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLogin(String login); 
    Optional<AppUser> findByEmail(String email);
    boolean existsByLogin(String login);         
    boolean existsByEmail(String email);
}