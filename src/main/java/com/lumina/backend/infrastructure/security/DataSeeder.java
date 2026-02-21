package com.lumina.backend.infrastructure.security;

import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@lumina.com}")
    private String adminEmail;

    @Value("${app.admin.password:tempPassword123}")
    private String adminPassword;

    @Value("${app.seeder.enabled:false}")
    private boolean seederEnabled;

    @Override
    public void run(String... args) {
        if (!seederEnabled) {
            return;
        }

        createAdminUserIfNotFound();
    }

    private void createAdminUserIfNotFound() {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("✅ Admin user already exists: {}", adminEmail);
            return;
        }

        log.info("🚀 Creating System Admin User...");

        User admin = User.builder()
                .fullName("System Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .nationalId("ADMIN001")
                .phoneNumber("+905550000000")
                .imageUrl("https://ui-avatars.com/api/?name=System+Admin&background=0D8ABC&color=fff")
                .build();

        userRepository.save(admin);
        log.info("✅ Admin user created successfully.");
    }
}