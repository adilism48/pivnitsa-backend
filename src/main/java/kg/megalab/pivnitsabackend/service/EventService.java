package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.event.EventBannerResponse;
import kg.megalab.pivnitsabackend.dto.event.EventBannersResponse;
import kg.megalab.pivnitsabackend.dto.event.EventRequest;
import kg.megalab.pivnitsabackend.dto.event.EventResponse;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
                        entity.getBannerUrl(),
                        entity.getStartsAt()
                ))
                .toList();

        return new EventBannersResponse(eventBannersResponse);
    }

    public EventResponse create(EventRequest request) {

        String bannerUrlToDb = s3FileStorageService.upload(request.file(), "banners");

        try {
            Event event = Event.builder()
                    .title(request.title())
                    .description(request.description())
                    .bannerUrl(bannerUrlToDb)
                    .status(request.status())
                    .startsAt(request.startsAt())
                    .endsAt(request.endsAt())
                    .build();

            event = eventRepository.save(event);

            return new EventResponse(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    s3FileStorageService.toFullUrl(bannerUrlToDb),
                    event.getStatus(),
                    event.getStartsAt(),
                    event.getEndsAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
        } catch (Exception e) {
            if (bannerUrlToDb != null) {
                // TODO: delete from aws
            }
            throw e;
        }
    }
}
