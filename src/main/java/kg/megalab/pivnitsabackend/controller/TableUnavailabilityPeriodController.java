package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.table.*;
import kg.megalab.pivnitsabackend.service.TableUnavailabilityPeriodService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TableUnavailabilityPeriodController {
    private final TableUnavailabilityPeriodService tableUnavailabilityPeriodService;

    @PostMapping("/{tableId}/unavailability")
    public ResponseEntity<UnavailabilityPeriodResponse> createPeriod(@PathVariable Long tableId, @Valid @RequestBody UnavailabilityPeriodRequest request) {
        UnavailabilityPeriodResponse response = tableUnavailabilityPeriodService.createPeriod(tableId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unavailability/{periodId}")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long periodId) {
        tableUnavailabilityPeriodService.deletePeriod(periodId);
        return ResponseEntity.noContent().build();
    }
}
