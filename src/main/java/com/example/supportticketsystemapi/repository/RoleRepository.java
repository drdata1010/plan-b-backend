package com.example.supportticketsystemapi.repository;

import com.example.supportticketsystemapi.entity.ERole;
import com.example.supportticketsystemapi.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
