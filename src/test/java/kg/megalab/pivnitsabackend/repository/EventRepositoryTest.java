package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.BaseIntegrationTest;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldReturnOnlyUpcomingPublishedEventsWithPagination() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.of(
                2026, 8, 10,
                12, 0, 0, 0,
                ZoneOffset.UTC
        );

        Event first = createEvent(
                "First event",
                EventStatus.PUBLISHED,
                now.plusDays(1)
        );

        Event second = createEvent(
                "Second event",
                EventStatus.PUBLISHED,
                now.plusDays(2)
        );

        Event past = createEvent(
                "Past event",
                EventStatus.PUBLISHED,
                now.minusDays(1)
        );

        Event draft = createEvent(
                "Draft event",
                EventStatus.DRAFT,
                now.plusDays(3)
        );

        eventRepository.saveAll(
                List.of(first, second, past, draft)
        );

        // Act
        Page<Event> firstPage =
                eventRepository.findUpcomingPublishedEventsPage(
                        now,
                        PageRequest.of(0, 1)
                );

        Page<Event> secondPage =
                eventRepository.findUpcomingPublishedEventsPage(
                        now,
                        PageRequest.of(1, 1)
                );

        // Assert
        assertThat(firstPage.getContent())
                .extracting(Event::getTitle)
                .containsExactly("First event");

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.getContent())
                .extracting(Event::getTitle)
                .containsExactly("Second event");

        assertThat(secondPage.hasNext()).isFalse();
    }

    private Event createEvent(
            String title,
            EventStatus status,
            OffsetDateTime startsAt
    ) {
        return Event.builder()
                .title(title)
                .status(status)
                .startsAt(startsAt)
                .build();
    }
}