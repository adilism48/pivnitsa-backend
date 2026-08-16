package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.Staff;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginRequest;
import kg.megalab.pivnitsabackend.dto.staff.StaffLoginResponse;
import kg.megalab.pivnitsabackend.repository.StaffRepository;
import kg.megalab.pivnitsabackend.exception.InvalidStaffCredentialsException;
import kg.megalab.pivnitsabackend.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}

