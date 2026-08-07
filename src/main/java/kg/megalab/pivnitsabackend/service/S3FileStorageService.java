package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService {

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10 МБ
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );


    private final S3Client s3Client;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.public-url:}")
    private String publicUrl;

    public String upload(MultipartFile file, String folder) {

        validateImage(file);

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
            throw new InvalidFileException("Ошибка при чтении файла");
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

    private void validateImage(MultipartFile file) {

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("Размер файла превышает допустимый лимит в 10 МБ");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("Неподдерживаемый формат файла. Разрешены только JPEG, PNG, GIF, WEBP");
        }

        if (!isRealImage(file)) {
            throw new InvalidFileException("Загруженный файл не является валидным изображением");
        }
    }

    private boolean isRealImage(MultipartFile file) {
        try (InputStream input = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(input)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return false;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return width > 0 && height > 0;
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
