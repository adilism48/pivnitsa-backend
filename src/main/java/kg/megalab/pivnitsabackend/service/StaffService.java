package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.staff.CreateStaffRequest;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginRequest;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginResponse;
import kg.megalab.pivnitsabackend.dto.staff.StaffResponse;
import kg.megalab.pivnitsabackend.entity.Staff;
import kg.megalab.pivnitsabackend.entity.StaffRole;
import kg.megalab.pivnitsabackend.exception.staffexception.*;
import kg.megalab.pivnitsabackend.repository.StaffRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public StaffLoginResponse login(StaffLoginRequest request) {
        Staff staff = staffRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidStaffCredentialsException("Неверный email или пароль"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), staff.getPasswordHash());

        if (!passwordMatches || !staff.isActive()) {
            throw new InvalidStaffCredentialsException("Неверный email или пароль");
        }

        String token = jwtService.generateStaffToken(staff.getEmail(), staff.getRole().name());

        return new StaffLoginResponse(token, staff.getFullName(), staff.getRole().name());
    }

    public StaffResponse createStaff(CreateStaffRequest request) {
        if (staffRepository.findByEmail(request.email()).isPresent()) {
            throw new StaffEmailAlreadyExistException("Сотрудник с таким email уже существует");
        }

        StaffRole role = parseRole(request.role());

        Staff staff = Staff.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        try {
            staff = staffRepository.save(staff);
        } catch (DataIntegrityViolationException e) {
            throw new StaffEmailAlreadyExistException("Сотрудник с таким email уже существует");
        }

        return toResponse(staff);
    }

    public void deactivateStaff(Long targetId, String actingEmail) {
        Staff target = staffRepository.findById(targetId)
                .orElseThrow(() -> new StaffNotFoundException("Сотрудник не найден"));

        if (!target.isActive()) {
            return;
        }

        Staff actingStaff = staffRepository.findByEmail(actingEmail)
                .orElseThrow(() -> new StaffNotFoundException("Сотрудник не найден"));

        if (actingStaff.getRole() == StaffRole.STAFF && target.getRole() == StaffRole.OWNER) {
            throw new InsufficientStaffPermissionException("Недостаточно прав для этого действия");
        }

        if (target.getRole() == StaffRole.OWNER) {
            long activeOwners = staffRepository.countByRoleAndActiveTrue(StaffRole.OWNER);
            if (activeOwners <= 1) {
                throw new LastOwnerException("Нельзя деактивировать последнего активного владельца");
            }
        }

        target.setActive(false);
        staffRepository.save(target);
    }

    public StaffResponse setFinancialAccess(Long targetId, boolean canView) {
        Staff target = staffRepository.findById(targetId)
                .orElseThrow(() -> new StaffNotFoundException("Сотрудник не найден"));

        target.setCanViewFinancialReport(canView);
        staffRepository.save(target);

        return toResponse(target);
    }

    private StaffRole parseRole(String role) {
        try {
            return StaffRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStaffRoleException("Некорректная роль: " + role + ". Допустимые значения: STAFF, OWNER");
        }
    }

    private StaffResponse toResponse(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getFullName(),
                staff.getEmail(),
                staff.getRole().name(),
                staff.isCanViewFinancialReport(),
                staff.isActive()
        );
    }
}