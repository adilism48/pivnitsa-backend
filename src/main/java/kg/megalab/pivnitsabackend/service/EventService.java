package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.event.*;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.exception.EventNotFoundException;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final S3FileStorageService s3FileStorageService;


    public EventBannersResponse getEventBanners(int limit) {

        int safeLimit = Math.clamp(limit, 1, 50);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Pageable pageable = PageRequest.of(0, safeLimit);

        List<Event> events = eventRepository.findUpcomingPublishedEvents(now, pageable);

        List<EventBannerResponse> eventBannersResponse = events.stream()
                .map(entity -> new EventBannerResponse(
                        entity.getId(),
                        entity.getTitle(),
                        s3FileStorageService.toFullUrl(entity.getBannerUrl()),
                        entity.getStartsAt()
                ))
                .toList();

        return new EventBannersResponse(eventBannersResponse);
    }

    public EventResponse create(CreateEventRequest request) {

        String bannerUrl = s3FileStorageService.upload(request.file(), "banners");

        try {
            Event event = Event.builder()
                    .title(request.title())
                    .description(request.description())
                    .bannerUrl(bannerUrl)
                    .status(request.status())
                    .startsAt(request.startsAt())
                    .endsAt(request.endsAt())
                    .build();

            event = eventRepository.save(event);

            return new EventResponse(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    s3FileStorageService.toFullUrl(bannerUrl),
                    event.getStatus(),
                    event.getStartsAt(),
                    event.getEndsAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
        } catch (Exception e) {
            s3FileStorageService.delete(bannerUrl);
            throw e;
        }
    }

    public EventResponse update(Long id, UpdateEventRequest request) {

        Event event = getEvent(id);

        String oldBannerUrl = event.getBannerUrl();
        String newBannerUrl = null;

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStatus(request.status());
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());

        boolean hasNewFile = request.file() != null && !request.file().isEmpty();

        if (hasNewFile) {
            newBannerUrl = s3FileStorageService.upload(request.file(), "banners");
            event.setBannerUrl(newBannerUrl);
        }

        try {
            event = eventRepository.save(event);

            if (hasNewFile && oldBannerUrl != null) {
                s3FileStorageService.delete(oldBannerUrl);
            }

            return new EventResponse(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    s3FileStorageService.toFullUrl(event.getBannerUrl()),
                    event.getStatus(),
                    event.getStartsAt(),
                    event.getEndsAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
        } catch (Exception e) {
            if (newBannerUrl != null) {
                s3FileStorageService.delete(newBannerUrl);
            }
            throw e;
        }
    }

    public void delete(Long id) {

        Event event = getEvent(id);

        String bannerKey = event.getBannerUrl();

        eventRepository.delete(event);

        if (bannerKey != null && !bannerKey.isBlank()) {
            s3FileStorageService.delete(bannerKey);
        }
    }

    @Transactional(readOnly = true)
    public EventPageResponse getUpcomingEvents(int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 50);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Page<Event> result =
                eventRepository.findUpcomingPublishedEventsPage(now, pageable);

        List<EventListItemResponse> items = result.getContent()
                .stream()
                .map(event -> new EventListItemResponse(
                        event.getId(),
                        event.getTitle(),
                        s3FileStorageService.toFullUrl(event.getBannerUrl()),
                        event.getStartsAt()
                ))
                .toList();

        return new EventPageResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.hasNext()
        );
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found, id: " + id));
    }


}
