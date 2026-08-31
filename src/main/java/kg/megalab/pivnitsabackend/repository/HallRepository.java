package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    Optional<Hall> findByName(String name);

}
