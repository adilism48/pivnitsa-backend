package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.ClubTable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubTableRepository extends JpaRepository<ClubTable, Long> {
    Optional<ClubTable> findByTableNumber(String tableNumber);

    List<ClubTable> findByActive(boolean active);
}