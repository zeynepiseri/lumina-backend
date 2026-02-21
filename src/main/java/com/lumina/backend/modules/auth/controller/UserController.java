package com.lumina.backend.modules.auth.controller;

import com.lumina.backend.modules.auth.dto.ChangePasswordRequest;
import com.lumina.backend.modules.auth.dto.UserResponse;
import com.lumina.backend.modules.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/photo")
    public ResponseEntity<Map<String, String>> updateProfilePhoto(
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        String newImageUrl = request.get("imageUrl");
        userService.updateProfileImage(principal.getName(), newImageUrl);

        return ResponseEntity.ok(Collections.singletonMap("message", "Profile Photo updated"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        UserResponse user = userService.getUserResponse(principal.getName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        userService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(Collections.singletonMap("message", "Şifre başarıyla değiştirildi."));
    }
}