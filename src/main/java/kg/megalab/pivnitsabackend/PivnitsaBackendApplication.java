package kg.megalab.pivnitsabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PivnitsaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PivnitsaBackendApplication.class, args);
    }

}
