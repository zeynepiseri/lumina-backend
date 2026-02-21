package com.lumina.backend.modules.auth.repository;

import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail - Should return user when exists")
    void findByEmail_ShouldReturnUser() {
         
        User user = User.builder()
                .email("test@lumina.com")
                .fullName("Test User")
                .nationalId("12345678900")
                .password("pass")
                .role(Role.PATIENT)
                .build();
        userRepository.save(user);

         
        Optional<User> found = userRepository.findByEmail("test@lumina.com");

         
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("save - Should throw exception when email is duplicate")
    void save_ShouldThrowException_WhenEmailDuplicate() {
        User user1 = User.builder()
                .email("duplicate@lumina.com")
                .nationalId("11111111111")
                .password("pass")
                .role(Role.PATIENT)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("duplicate@lumina.com")  
                .nationalId("22222222222")
                .password("pass")
                .role(Role.PATIENT)
                .build();

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}