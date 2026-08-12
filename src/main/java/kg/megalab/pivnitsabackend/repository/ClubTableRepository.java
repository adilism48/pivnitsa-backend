package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.ClubTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubTableRepository extends JpaRepository<ClubTable, Long> {
}