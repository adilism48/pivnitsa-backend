package kg.megalab.pivnitsabackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "club_tables")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(name = "table_number", nullable = false, unique = true)
    private String tableNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "position_x", precision = 5, scale = 2)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 5, scale = 2)
    private BigDecimal positionY;

    @Column(name = "category")
    private String category;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}