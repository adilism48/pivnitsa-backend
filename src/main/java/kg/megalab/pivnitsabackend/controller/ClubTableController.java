package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.table.*;
import kg.megalab.pivnitsabackend.service.ClubTableService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kg.megalab.pivnitsabackend.service.TableStatusService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ClubTableController {
    private final ClubTableService clubTableService;
    private final TableStatusService tableStatusService;

    @GetMapping
    public ResponseEntity<List<PublicTableResponse>> getTables(@RequestParam(required = false) Long hallId) {
        List<PublicTableResponse> response = clubTableService.getPublicTables(hallId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<List<TableStatusResponse>> getTableStatuses(@RequestParam LocalDate date, @RequestParam Integer guestsCount, @RequestParam(required = false) Long hallId) {
        List<TableStatusResponse> response = tableStatusService.getTableStatuses(date, guestsCount, hallId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@Valid @RequestBody CreateTableRequest request) {
        TableResponse response = clubTableService.createTable(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TableResponse> updateTable(@PathVariable Long id, @Valid @RequestBody UpdateTableRequest request) {
        TableResponse response = clubTableService.updateTable(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        clubTableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}