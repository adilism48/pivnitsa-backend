package kg.megalab.pivnitsabackend.otp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Order(2)
@Component
public class InfobipOtpSender implements OtpSender {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${otp.infobip.enabled:false}")
    private boolean enabled;

    @Value("${otp.infobip.base-url:}")
    private String baseUrl;

    @Value("${otp.infobip.api-key:}")
    private String apiKey;

    @Value("${otp.infobip.sender:Pivnitsa}")
    private String sender;

    @Override
    public boolean supports(NotificationChannel channel) {
        // Резерв для SMS + основной канал для WhatsApp/Email (когда появится их реализация)
        return channel == NotificationChannel.SMS;
    }

    @Override
    public void send(String phone, String code) {
        if (!enabled) {
            log.info("[Infobip STUB] Код для {}: {}", phone, code);
            return;
        }

        String text = "Ваш код подтверждения Pivnitsa: " + code;
        String phoneDigitsOnly = phone.replace("+", "");

        Map<String, Object> requestBody = Map.of(
                "messages", List.of(
                        Map.of(
                                "sender", sender,
                                "destinations", List.of(Map.of("to", phoneDigitsOnly)),
                                "content", Map.of("text", text)
                        )
                )
        );

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/sms/3/messages"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "App " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OtpSendException(
                        "Infobip вернул HTTP " + response.statusCode() + ": " + response.body(), null
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            String groupName = root.path("messages").path(0).path("status").path("groupName").asText();

            if ("REJECTED".equals(groupName) || "UNDELIVERABLE".equals(groupName)) {
                throw new OtpSendException("Infobip отклонил сообщение: " + groupName, null);
            }

        } catch (OtpSendException e) {
            throw e;
        } catch (Exception e) {
            throw new OtpSendException("Не удалось отправить SMS через Infobip", e);
        }
    }
}