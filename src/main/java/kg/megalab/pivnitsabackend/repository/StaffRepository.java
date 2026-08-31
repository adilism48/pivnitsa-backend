package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Staff;
import kg.megalab.pivnitsabackend.entity.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);

    long countByRoleAndActiveTrue(StaffRole role);
}
