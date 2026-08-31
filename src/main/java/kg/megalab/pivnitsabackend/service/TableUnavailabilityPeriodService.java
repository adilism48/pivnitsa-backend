package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.table.UnavailabilityPeriodResponse;
import kg.megalab.pivnitsabackend.dto.table.UnavailabilityPeriodRequest;
import kg.megalab.pivnitsabackend.entity.TableUnavailabilityPeriod;
import kg.megalab.pivnitsabackend.exception.tables.*;
import kg.megalab.pivnitsabackend.exception.tables.*;
import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.repository.TableUnavailabilityPeriodRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableUnavailabilityPeriodService {
    private final TableUnavailabilityPeriodRepository tableUnavailabilityPeriodRepository;
    private final ClubTableRepository clubTableRepository;

    @Transactional
    public UnavailabilityPeriodResponse createPeriod(Long tableId, UnavailabilityPeriodRequest request) {
        if (clubTableRepository.findById(tableId).isEmpty()) {
            throw new TableNotFoundException("Столик не найден");
        }

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new InvalidPeriodException("Дата окончания должна быть позже даты начала");
        }

        TableUnavailabilityPeriod period = TableUnavailabilityPeriod.builder()
                .clubTableId(tableId)
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .reason(request.reason())
                .build();

        period = tableUnavailabilityPeriodRepository.save(period);

        return toResponse(period);
    }

    @Transactional
    public void deletePeriod(Long periodId) {
        if (tableUnavailabilityPeriodRepository.findById(periodId).isEmpty()) {
            throw new UnavailabilityPeriodNotFoundException("Период недоступности не найден");
        }
        tableUnavailabilityPeriodRepository.deleteById(periodId);
    }

    private UnavailabilityPeriodResponse toResponse(TableUnavailabilityPeriod period) {
        return new UnavailabilityPeriodResponse(
                period.getId(),
                period.getClubTableId(),
                period.getStartsAt(),
                period.getEndsAt(),
                period.getReason()
        );
    }
}
