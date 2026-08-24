package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.TableUnavailabilityPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableUnavailabilityPeriodRepository extends JpaRepository<TableUnavailabilityPeriod, Long> {
    
}