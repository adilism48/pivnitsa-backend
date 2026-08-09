package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
