package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.table.CreateTableRequest;
import kg.megalab.pivnitsabackend.dto.table.TableResponse;
import kg.megalab.pivnitsabackend.entity.ClubTable;
import kg.megalab.pivnitsabackend.exception.hall.HallNotFoundException;
import kg.megalab.pivnitsabackend.exception.tables.TableNumberAlreadyExistException;
import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.repository.HallRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubTableService {

    private final ClubTableRepository clubTableRepository;
    private final HallRepository hallRepository;

    @Transactional
    public TableResponse createTable(CreateTableRequest request) {
        if (clubTableRepository.findByTableNumber(request.tableNumber()).isPresent()) {
            throw new TableNumberAlreadyExistException("Такой стол уже существует");
        }

        if (hallRepository.findById(request.hallId()).isEmpty()) {
            throw new HallNotFoundException("Такого зала не существует");
        }

        ClubTable clubTable = ClubTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .hallId(request.hallId())
                .build();

        clubTable = clubTableRepository.save(clubTable);

        return toResponse(clubTable);
    }

    private TableResponse toResponse(ClubTable clubTable) {
        return new TableResponse(
                clubTable.getId(),
                clubTable.getTableNumber(),
                clubTable.getCapacity(),
                clubTable.getHallId(),
                clubTable.isActive(),
                clubTable.getPositionX(),
                clubTable.getPositionY(),
                clubTable.getCategory(),
                clubTable.getDepositAmount()
        );
    }
}