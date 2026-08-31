package kg.megalab.pivnitsabackend.dto.table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TableResponse(
        Long id,
        String tableNumber,
        Integer capacity,
        Long hallId,
        boolean active,
        BigDecimal positionX,
        BigDecimal positionY,
        String category,
        BigDecimal depositAmount
) {
}