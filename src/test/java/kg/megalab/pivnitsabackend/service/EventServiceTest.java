package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.event.EventPageResponse;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private S3FileStorageService s3FileStorageService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(
                eventRepository,
                s3FileStorageService
        );
    }

    @Test
    void shouldReturnUpcomingEventsPage() {
        // Arrange
        OffsetDateTime startsAt =
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        Event event = Event.builder()
                .id(1L)
                .title("DJ Night Party")
                .bannerUrl("banners/event.jpg")
                .status(EventStatus.PUBLISHED)
                .startsAt(startsAt)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Event> repositoryResult = new PageImpl<>(
                List.of(event),
                pageable,
                11
        );

        when(eventRepository.findUpcomingPublishedEventsPage(
                any(OffsetDateTime.class),
                eq(pageable)
        )).thenReturn(repositoryResult);

        when(s3FileStorageService.toFullUrl("banners/event.jpg"))
                .thenReturn("http://localhost:3902/banners/event.jpg");

        // Act
        EventPageResponse response =
                eventService.getUpcomingEvents(0, 10);

        // Assert
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertTrue(response.hasNext());
        assertEquals(1, response.items().size());

        var item = response.items().getFirst();

        assertEquals(1L, item.id());
        assertEquals("DJ Night Party", item.title());
        assertEquals(
                "http://localhost:3902/banners/event.jpg",
                item.bannerUrl()
        );
        assertEquals(startsAt, item.startsAt());

        verify(eventRepository).findUpcomingPublishedEventsPage(
                any(OffsetDateTime.class),
                eq(pageable)
        );

        verify(s3FileStorageService)
                .toFullUrl("banners/event.jpg");
    }

    @Test
    void shouldNormalizeInvalidPaginationParameters() {
        // Arrange
        PageRequest expectedPageable = PageRequest.of(0, 1);

        when(eventRepository.findUpcomingPublishedEventsPage(
                any(OffsetDateTime.class),
                eq(expectedPageable)
        )).thenReturn(Page.empty(expectedPageable));

        // Act
        EventPageResponse response =
                eventService.getUpcomingEvents(-5, 0);

        // Assert
        assertEquals(0, response.page());
        assertEquals(1, response.size());
        assertFalse(response.hasNext());
        assertTrue(response.items().isEmpty());

        verify(eventRepository).findUpcomingPublishedEventsPage(
                any(OffsetDateTime.class),
                eq(expectedPageable)
        );
    }
}