package kg.megalab.pivnitsabackend.dto.table;

import kg.megalab.pivnitsabackend.entity.TableStatus;

import java.math.BigDecimal;

public record TableStatusResponse(
        Long id,
        String tableNumber,
        Integer capacity,
        TableStatus status,
        boolean fitsCapacity,
        BigDecimal positionX,
        BigDecimal positionY,
        Long hallId,
        String category
) {
}
