package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.table.*;
import kg.megalab.pivnitsabackend.service.ClubTableService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ClubTableController {
    private final ClubTableService clubTableService;

    @GetMapping
    public ResponseEntity<List<PublicTableResponse>> getTables(@RequestParam(required = false) Long hallId) {
        List<PublicTableResponse> response = clubTableService.getPublicTables(hallId);
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