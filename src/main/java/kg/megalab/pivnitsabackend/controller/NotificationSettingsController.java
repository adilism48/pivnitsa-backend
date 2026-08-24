package kg.megalab.pivnitsabackend.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.notification.NotificationSettingsResponse;
import kg.megalab.pivnitsabackend.dto.notification.UpdateNotificationSettingsRequest;
import kg.megalab.pivnitsabackend.service.notifications.UserNotificationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/notification-settings")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class NotificationSettingsController {

    private final UserNotificationSettingsService settingsService;

    @GetMapping
    public ResponseEntity<NotificationSettingsResponse> getSettings(
            @AuthenticationPrincipal String phone
    ) {
        return ResponseEntity.ok(
                settingsService.getCurrentUserSettings(phone)
        );
    }

    @PutMapping
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @AuthenticationPrincipal String phone,
            @Valid @RequestBody UpdateNotificationSettingsRequest request
    ) {
        return ResponseEntity.ok(
                settingsService.updateCurrentUserSettings(phone, request)
        );
    }
}
