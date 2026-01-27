package com.andreyk.practiceproject.core.service.impl;

import com.andreyk.practiceproject.api.dto.storage.StorageItemDto;
import com.andreyk.practiceproject.api.dto.storage.StorageItemType;
import com.andreyk.practiceproject.api.dto.storage.StorageObjectDetailsDto;
import com.andreyk.practiceproject.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                    .withZone(ZoneId.systemDefault());

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Override
    public List<StorageItemDto> listObjects(String path) {
        return fetchObjects(path);
    }

    @Override
    public List<StorageItemDto> listObjects(String name, String type, LocalDate modifiedFrom, LocalDate modifiedTo) {
        return fetchObjects("")
                .stream()
                .filter(byName(name))
                .filter(byType(type))
                .filter(byModifiedDate(modifiedFrom, modifiedTo))
                .toList();
    }

    @Override
    public List<StorageItemDto> search(String name, String path, String type) {
        return fetchObjects(path)
                .stream()
                .filter(item -> item.getName().contains(name))
                .filter(item -> matchesType(item, type))
                .toList();
    }

    @Override
    public StorageObjectDetailsDto getObjectDetails(String path) {
        StorageItemType type = resolveType(path);

        return type == StorageItemType.FILE
                ? fileDetails(path)
                : directoryDetails(path);
    }

    /* ===================== DETAILS ===================== */

    private StorageObjectDetailsDto fileDetails(String path) {

        HeadObjectResponse response = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .build()
        );

        return new StorageObjectDetailsDto(
                extractName(path),
                path,
                StorageItemType.FILE,
                format(response.lastModified()),
                response.contentLength()
        );
    }

    private StorageObjectDetailsDto directoryDetails(String path) {

        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .maxKeys(1)
                        .build()
        );

        if (response.contents().isEmpty()) {
            throw new IllegalArgumentException("Directory not found: " + path);
        }

        return new StorageObjectDetailsDto(
                extractName(path),
                path,
                StorageItemType.DIRECTORY,
                format(response.contents().get(0).lastModified()),
                null
        );
    }

    /* ===================== MAPPING ===================== */

    private StorageItemDto toItemDto(S3Object object) {
        String key = object.key();
        StorageItemType type = resolveType(key);

        return new StorageItemDto(
                extractName(key),
                key,
                type,
                format(object.lastModified())
        );
    }

    private List<StorageItemDto> fetchObjects(String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        return s3Client.listObjectsV2(request)
                .contents()
                .stream()
                .map(this::toItemDto)
                .toList();
    }

    /* ===================== HELPERS ===================== */

    private boolean matchesType(StorageItemDto item, String type) {
        if (type == null) {
            return true;
        }

        return item.getType().name().equalsIgnoreCase(type);
    }

    private StorageItemType resolveType(String path) {
        return path.endsWith("/")
                ? StorageItemType.DIRECTORY
                : StorageItemType.FILE;
    }

    private String extractName(String path) {
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int idx = trimmed.lastIndexOf('/');
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    private String format(Instant instant) {
        return instant == null ? null : FORMATTER.format(instant);
    }

    private Predicate<StorageItemDto> byName(String name) {
        return item -> name == null || item.getName().contains(name);
    }

    private Predicate<StorageItemDto> byType(String type) {
        return item -> type == null
                || item.getType().name().equalsIgnoreCase(type);
    }

    private Predicate<StorageItemDto> byModifiedDate(
            LocalDate from,
            LocalDate to
    ) {
        return item -> {
            if (from == null && to == null) return true;

            LocalDate itemDate = parseDate(item.getLastModified());

            if (from != null && itemDate.isBefore(from)) return false;
            return to == null || !itemDate.isAfter(to);
        };
    }

    private LocalDate parseDate(String formattedDate) {
        return LocalDate.parse(
                formattedDate.substring(0, 10).replace('.', '-')
        );
    }
}
