package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.TableStatus;
import kg.megalab.pivnitsabackend.entity.TableUnavailabilityPeriod;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.TableUnavailabilityPeriodRepository;
import kg.megalab.pivnitsabackend.dto.table.TableStatusResponse;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TableStatusService {
    private final BookingRepository bookingRepository;
    private final TableUnavailabilityPeriod unavailabilityPeriod;


}
