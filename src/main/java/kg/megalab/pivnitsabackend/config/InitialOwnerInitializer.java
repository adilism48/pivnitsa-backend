package kg.megalab.pivnitsabackend.config;

import kg.megalab.pivnitsabackend.entity.Staff;
import kg.megalab.pivnitsabackend.entity.StaffRole;
import kg.megalab.pivnitsabackend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialOwnerInitializer implements ApplicationRunner {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-owner.email:}")
    private String ownerEmail;

    @Value("${app.initial-owner.password:}")
    private String ownerPassword;

    @Value("${app.initial-owner.full-name:Владелец}")
    private String ownerFullName;

    @Override
    public void run(ApplicationArguments args) {
        long activeOwners = staffRepository.countByRoleAndActiveTrue(StaffRole.OWNER);

        if (activeOwners > 0) {
            return;
        }

        if (ownerEmail.isBlank() || ownerPassword.isBlank()) {
            log.warn("В системе нет ни одного активного OWNER, но переменные окружения, первый владелец не создан");
            return;
        }

        Staff owner = Staff.builder()
                .fullName(ownerFullName)
                .email(ownerEmail)
                .passwordHash(passwordEncoder.encode(ownerPassword))
                .role(StaffRole.OWNER)
                .build();

        staffRepository.save(owner);

        log.info("Создан первый владелец системы: {}", ownerEmail);
    }
}