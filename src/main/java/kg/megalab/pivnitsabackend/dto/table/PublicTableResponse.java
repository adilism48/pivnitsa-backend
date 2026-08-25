package kg.megalab.pivnitsabackend.dto.table;


import java.math.BigDecimal;

public record PublicTableResponse(
        Long id,
        String tableNumber,
        Integer capacity,
        BigDecimal positionX,
        BigDecimal positionY,
        Long hallId,
        String category
) {

}
