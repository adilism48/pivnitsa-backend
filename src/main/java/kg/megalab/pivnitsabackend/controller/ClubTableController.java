package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.config.OpenApiConfig;
import kg.megalab.pivnitsabackend.dto.table.*;
import kg.megalab.pivnitsabackend.service.ClubTableService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ClubTableController {
    private final ClubTableService clubTableService;

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

}