package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String phone = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUser(phone));
    }
}
