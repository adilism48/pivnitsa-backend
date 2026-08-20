package kg.megalab.pivnitsabackend;

import kg.megalab.pivnitsabackend.config.PaymentRedirectProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaymentRedirectProperties.class)
public class PivnitsaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PivnitsaBackendApplication.class, args);
    }

}
