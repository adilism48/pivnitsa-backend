package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.BaseIntegrationTest;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventListE2ETest extends BaseIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldReturnUpcomingEventsWithoutAuthentication() throws Exception {
        // Arrange
        Event event = Event.builder()
                .title("DJ Night Party")
                .description("Nightclub event")
                .bannerUrl("banners/event.jpg")
                .status(EventStatus.PUBLISHED)
                .startsAt(
                        OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
                )
                .build();

        eventRepository.saveAndFlush(event);

        // Act + Assert
        mockMvc.perform(get("/api/v1/events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(event.getId()))
                .andExpect(jsonPath("$.items[0].title")
                        .value("DJ Night Party"))
                .andExpect(jsonPath("$.items[0].bannerUrl")
                        .value(endsWith("/banners/event.jpg")));
    }
}