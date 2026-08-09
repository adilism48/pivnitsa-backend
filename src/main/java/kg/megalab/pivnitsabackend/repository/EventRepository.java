package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
                SELECT e FROM Event e
                WHERE e.status = kg.megalab.pivnitsabackend.entity.EventStatus.PUBLISHED
                  AND e.startsAt >= :now
                ORDER BY e.startsAt ASC
           """)
    List<Event> findUpcomingPublishedEvents(@Param("now") OffsetDateTime now, Pageable pageable);

    @Query("""
                SELECT e FROM Event e
                WHERE e.status = kg.megalab.pivnitsabackend.entity.EventStatus.PUBLISHED
                AND e.startsAt >= :now
                ORDER BY e.startsAt ASC, e.id ASC
           """)
    Page<Event> findUpcomingPublishedEventsPage(
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
