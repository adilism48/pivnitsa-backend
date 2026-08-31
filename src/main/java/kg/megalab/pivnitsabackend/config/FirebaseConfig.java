package kg.megalab.pivnitsabackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${app.firebase.config-path:classpath:firebase-service-account.json}")
    private Resource firebaseResource;

    @Bean
    @Nullable
    public FirebaseApp firebaseApp() {

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try {
            if (firebaseResource != null && firebaseResource.exists()) {
                try (InputStream serviceAccount = firebaseResource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    log.info("FirebaseApp успешно инициализирован из: {}", firebaseResource.getDescription());
                    return FirebaseApp.initializeApp(options);
                }
            } else {
                log.warn("Конфигурационный файл Firebase не найден: {}. Запуск продолжается без Firebase.", firebaseResource);
            }
        } catch (Exception e) {
            log.error("Firebase init failed", e);
        }
        return null;
    }

    @Bean
    @Nullable
    public FirebaseMessaging firebaseMessaging(@Nullable FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            return null;
        }
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}