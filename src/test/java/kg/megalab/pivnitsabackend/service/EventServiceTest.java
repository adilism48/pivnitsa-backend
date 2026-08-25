package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.event.CreateEventRequest;
import kg.megalab.pivnitsabackend.dto.event.EventPageResponse;
import kg.megalab.pivnitsabackend.dto.notification.EventPublishedEvent;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private S3FileStorageService s3FileStorageService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(
                eventRepository,
                s3FileStorageService,
                transactionTemplate,
                eventPublisher
        );

        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
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

    @Test
    void shouldPublishNotificationWhenEventCreatedAsPublishedFirstTime() {
        Event saved = Event.builder()
                .id(1L)
                .title("Concert")
                .status(EventStatus.PUBLISHED)
                .notificationSent(false)
                .build();

        when(s3FileStorageService.upload(any(), eq("banners")))
                .thenReturn("banners/concert.jpg");
        when(eventRepository.save(any(Event.class))).thenReturn(saved);
        when(eventRepository.markNotifiedIfNotAlready(1L)).thenReturn(1);

        // Act
        eventService.create(createRequest(EventStatus.PUBLISHED));

        // Assert
        verify(eventRepository).markNotifiedIfNotAlready(1L);
        verify(eventPublisher).publishEvent(any(EventPublishedEvent.class));
    }

    @Test
    void shouldNotPublishNotificationTwiceOnConcurrentUpdate() {
        Event saved = Event.builder()
                .id(1L)
                .title("Concert")
                .status(EventStatus.PUBLISHED)
                .notificationSent(false)
                .build();

        when(s3FileStorageService.upload(any(), eq("banners")))
                .thenReturn("banners/concert.jpg");
        when(eventRepository.save(any(Event.class))).thenReturn(saved);
        when(eventRepository.markNotifiedIfNotAlready(1L)).thenReturn(0);

        eventService.create(createRequest(EventStatus.PUBLISHED));

        verify(eventRepository).markNotifiedIfNotAlready(1L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPublishNotificationForDraftEvent() {
        Event saved = Event.builder()
                .id(1L)
                .title("Draft event")
                .status(EventStatus.DRAFT)
                .notificationSent(false)
                .build();

        when(s3FileStorageService.upload(any(), eq("banners")))
                .thenReturn("banners/draft.jpg");
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        eventService.create(createRequest(EventStatus.DRAFT));

        verify(eventRepository, never()).markNotifiedIfNotAlready(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private CreateEventRequest createRequest(EventStatus status) {
        return new CreateEventRequest(
                "Concert", "Description",
                new MockMultipartFile("file", "banner.jpg", "image/jpeg", new byte[]{1, 2, 3}),
                status,
                OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
        );
    }
}