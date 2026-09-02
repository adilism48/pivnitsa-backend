package kg.megalab.pivnitsabackend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.event.*;
import kg.megalab.pivnitsabackend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<EventPageResponse> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                eventService.getUpcomingEvents(page, size)
        );
    }

    @GetMapping("/banners")
    public ResponseEntity<EventBannersResponse> getEventBanners(@RequestParam(defaultValue = "3") int limit){

        EventBannersResponse eventBanners = eventService.getEventBanners(limit);

        return ResponseEntity.ok(eventBanners);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }
}