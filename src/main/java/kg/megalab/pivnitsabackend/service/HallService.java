package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.Hall;
import kg.megalab.pivnitsabackend.dto.hall.HallResponse;

import kg.megalab.pivnitsabackend.repository.HallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;

    public List<HallResponse> getAllHalls() {
        List<Hall> halls = hallRepository.findAll();

        return halls.stream()
                .map(hall -> new HallResponse(
                        hall.getId(),
                        hall.getName()
                )).toList();
    }
}
