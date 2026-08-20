package kg.megalab.pivnitsabackend.dto.table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateTableRequest(
        String tableNumber,

        @Positive(message = "Вместимость должна быть больше 0")
        Integer capacity,

        @Positive(message = "Некорректный id зала")
        Long hallId,

        Boolean active,
        String category,
        BigDecimal positionX,
        BigDecimal positionY,

        @Positive(message = "Сумма депозита должны быть больше 0")
        BigDecimal depositAmount

) {
}