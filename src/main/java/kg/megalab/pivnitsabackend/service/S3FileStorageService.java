package kg.megalab.pivnitsabackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService {

    private final S3Client s3Client;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.public-url:}")
    private String publicUrl;

    public String upload(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = String.format("%s/%s%s", folder, UUID.randomUUID(), extension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Файл успешно загружен в S3 с ключом: {}", key);

            return key;

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла для S3", e);
        }
    }

    public String toFullUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }

        String base = publicUrl.endsWith("/")
                ? publicUrl.substring(0, publicUrl.length() - 1)
                : publicUrl;

        String cleanKey = key.startsWith("/") ? key.substring(1) : key;

        return base + "/" + cleanKey;
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Файл успешно удален из S3: {}", key);
        } catch (Exception e) {
            log.error("Ошибка при удалении файла {} из S3", key, e);
        }
    }
}
