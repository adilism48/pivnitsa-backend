package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.event.EventBannersResponse;
import kg.megalab.pivnitsabackend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/banners")
    public ResponseEntity<EventBannersResponse> getEventBanners(@RequestParam(defaultValue = "3") int limit){

        EventBannersResponse eventBanners = eventService.getEventBanners(limit);

        return ResponseEntity.ok(eventBanners);
    }
}
