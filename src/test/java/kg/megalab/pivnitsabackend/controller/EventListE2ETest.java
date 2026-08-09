package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.entity.EventStatus;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EventListE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine")
            ).withExposedPorts(6379);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MockMvc mockMvc;

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