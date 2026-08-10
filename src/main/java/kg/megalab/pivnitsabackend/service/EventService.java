package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.event.EventResponse;
import kg.megalab.pivnitsabackend.entity.Event;
import kg.megalab.pivnitsabackend.exception.EventNotFoundException;
import kg.megalab.pivnitsabackend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Мероприятие не найдено"));

        return toResponse(event);
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getBannerUrl(),
                event.getStartsAt(),
                event.getEndsAt()
        );
    }
}