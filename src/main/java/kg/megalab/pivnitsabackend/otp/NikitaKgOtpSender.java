package kg.megalab.pivnitsabackend.otp;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Slf4j
@Order(1)
@Component
public class NikitaKgOtpSender implements OtpSender {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${otp.nikita.enabled:false}")
    private boolean enabled;

    @Value("${otp.nikita.api-url:https://smspro.nikita.kg/api/message}")
    private String apiUrl;

    @Value("${otp.nikita.login:}")
    private String login;

    @Value("${otp.nikita.password:}")
    private String password;

    @Value("${otp.nikita.sender:Pivnitsa}")
    private String sender;

    @Override
    public boolean supports(NotificationChannel channel) {
        // nikita.kg — основной провайдер именно для SMS
        return channel == NotificationChannel.SMS;
    }

    @Override
    public void send(String phone, String code) {
        if (!enabled) {
            log.info("[nikita.kg STUB] Код для {}: {}", phone, code);
            return;
        }

        String messageId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String phoneDigitsOnly = phone.replace("+", "");
        String text = "Ваш код подтверждения Pivnitsa: " + code;

        String xmlBody = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                <login>%s</login>
                <pwd>%s</pwd>
                <id>%s</id>
                <sender>%s</sender>
                <text>%s</text>
                <phones>
                <phone>%s</phone>
                </phones>
                </message>
                """.formatted(login, password, messageId, sender, text, phoneDigitsOnly);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(xmlBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String status = extractXmlValue(response.body(), "status");

            if (!"0".equals(status)) {
                throw new OtpSendException("nikita.kg вернул статус ошибки: " + status, null);
            }

        } catch (OtpSendException e) {
            throw e;
        } catch (Exception e) {
            throw new OtpSendException("Не удалось отправить SMS через nikita.kg", e);
        }
    }

    private String extractXmlValue(String xml, String tagName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList nodes = doc.getElementsByTagName(tagName);
            return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
        } catch (Exception e) {
            throw new OtpSendException("Не удалось разобрать ответ nikita.kg", e);
        }
    }
}