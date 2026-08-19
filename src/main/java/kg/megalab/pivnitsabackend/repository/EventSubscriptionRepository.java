package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {

    @Query("SELECT s.userId FROM EventSubscription s")
    List<Long> findAllSubscribedUserIds();
}
