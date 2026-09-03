package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.repository.ClubTableRepository;
import kg.megalab.pivnitsabackend.repository.TableUnavailabilityPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kg.megalab.pivnitsabackend.entity.ClubTable;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TableUnavailabilityChecker {
    private final ClubTableRepository clubTableRepository;
    private final TableUnavailabilityPeriodRepository tableUnavailabilityPeriodRepository;

    public Set<Long> findUnavailableTableIds(OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        List<ClubTable> inactiveTables = clubTableRepository.findByActive(false);

        List<Long> inactiveTableIds = inactiveTables.stream()
                .map(ClubTable::getId)
                .toList();

        List<Long> unavailablePeriodTableIds = tableUnavailabilityPeriodRepository
                .findClubTableIdsWithUnavailabilityPeriod(startOfDay, endOfDay);

        Set<Long> result = new HashSet<>();
        result.addAll(inactiveTableIds);
        result.addAll(unavailablePeriodTableIds);

        return result;
    }

    public boolean isUnavailable(ClubTable table, OffsetDateTime startOfDay, OffsetDateTime endOfDay) {
        if (!table.isActive()) {
            return true;
        }

        return tableUnavailabilityPeriodRepository.existsUnavailabilityPeriodForTable(
                table.getId(), startOfDay, endOfDay
        );
    }
}