package kg.megalab.pivnitsabackend.controller;

import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.dto.UpdateProfileRequest;
import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
            ){
        String currentPhone = authentication.getName();

        UserResponse response = userService.updateCurrentUser(
                currentPhone,
                request
        );

        return ResponseEntity.ok(response);
    }
}
