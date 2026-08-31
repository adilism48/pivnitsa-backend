package kg.megalab.pivnitsabackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.ZoneId;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.notifications.booking-reminder")
public class BookingReminderProperties {

    private boolean enabled = true;
    private Duration advance = Duration.ofHours(24);
    private Duration lookback = Duration.ofMinutes(5);
    private Duration lookahead = Duration.ofMinutes(5);
    private Duration fixedDelay = Duration.ofMinutes(5);
    private Duration initialDelay = Duration.ofMinutes(1);
    private ZoneId timezone = ZoneId.of("Asia/Bishkek");
}
