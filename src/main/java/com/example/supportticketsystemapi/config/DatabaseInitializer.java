package com.example.supportticketsystemapi.config;

import com.example.supportticketsystemapi.entity.ERole;
import com.example.supportticketsystemapi.entity.Role;
import com.example.supportticketsystemapi.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsById(role.ordinal() + 1)) {
                roleRepository.save(new Role(role));
            }
        }
    }
}
