package kg.megalab.pivnitsabackend.dto.table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTableRequest(
        @NotBlank(message = "Номер столика должен быть указан")
        String tableNumber,

        @NotNull(message = "Вместимость должна быть заполнена")
        @Positive(message = "Вместимость должна быть больше 0")
        Integer capacity,

        @NotNull(message = "Должен быть указан зал")
        @Positive(message = "Некорректный id зала")
        Long hallId
) {
}