package kg.megalab.pivnitsabackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(BookingReminderProperties.class)
public class BookingReminderConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
