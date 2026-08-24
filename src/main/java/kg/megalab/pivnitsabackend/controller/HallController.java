package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.service.HallService;
import kg.megalab.pivnitsabackend.dto.hall.HallResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/halls")
@RequiredArgsConstructor
public class HallController {
    private final HallService hallService;

    @GetMapping
    public ResponseEntity<List<HallResponse>> getAllHalls() {
        List<HallResponse> response = hallService.getAllHalls();
        return ResponseEntity.ok(response);
    }

}
