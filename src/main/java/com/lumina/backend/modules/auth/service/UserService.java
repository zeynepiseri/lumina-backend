package com.lumina.backend.modules.auth.service;

import com.lumina.backend.modules.auth.dto.ChangePasswordRequest;
import com.lumina.backend.modules.auth.dto.UserResponse;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateProfileImage(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.setImageUrl(imageUrl);
        userRepository.save(user);
    }

    public UserResponse getUserResponse(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return mapToResponse(user);
    }

    public User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .nationalId(user.getNationalId())
                .imageUrl(user.getImageUrl())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .build();
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mevcut şifre hatalı!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}