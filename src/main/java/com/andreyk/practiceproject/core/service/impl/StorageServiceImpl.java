package com.andreyk.practiceproject.core.service.impl;

import com.andreyk.practiceproject.api.dto.storage.*;
import com.andreyk.practiceproject.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        return fetchObjects(path, "current");
    }

    @Override
    public List<StorageItemDto> listObjects(String name, String type, LocalDate modifiedFrom, LocalDate modifiedTo) {
        return fetchObjects("", "all")
                .stream()
                .filter(byName(name))
                .filter(byType(type))
                .filter(byModifiedDate(modifiedFrom, modifiedTo))
                .toList();
    }

    @Override
    public List<StorageItemDto> search(String name, String path, String type, String depth) {
        return fetchObjects(path, depth)
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

    @Override
    public StorageDownloadDto download(String path) {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Cannot download directory");
        }

        ResponseInputStream<GetObjectResponse> s3Object =
                s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(path)
                                .build()
                );

        String filename = extractName(path);

        return new StorageDownloadDto(
                filename,
                s3Object.response().contentLength(),
                s3Object.response().contentType(),
                new InputStreamResource(s3Object)
        );
    }

    @Override
    public StorageDownloadDto downloadAsZip(List<String> paths) {
        paths.forEach(this::validatePathExists);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        Set<String> addedEntries = new HashSet<>();

        for (String path : paths) {
            addPath(zipOut, path, addedEntries);
        }

        if (addedEntries.isEmpty()) {
            throw new IllegalArgumentException("No files or directories to download");
        }

        try {
            zipOut.finish();
            zipOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] zipBytes = baos.toByteArray();

        return new StorageDownloadDto(
                "storage.zip",
                zipBytes.length,
                "application/zip",
                new InputStreamResource(new ByteArrayInputStream(zipBytes))
        );
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

    private StorageItemDto mapToStorageItem(
            String key,
            Instant lastModified
    ) {
        StorageItemType type = resolveType(key);

        return new StorageItemDto(
                extractName(key),
                key,
                type,
                format(lastModified)
        );
    }

    private List<StorageItemDto> fetchObjects(String prefix, String depth) {
        if (SearchDepth.CURRENT.name().equals(depth.toUpperCase())) {
            ListObjectsV2Response response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .delimiter("/")
                            .build()
            );
            return getStorageItemDtoList(prefix, response);
        }
        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build()
        );
        return getStorageItemDtoList(prefix, response);
    }

    private List<StorageItemDto> getStorageItemDtoList(String prefix, ListObjectsV2Response response) {
        List<StorageItemDto> files = getFiles(prefix, response);

        List<StorageItemDto> directories = getDirectories(response);

        return Stream.concat(files.stream(), directories.stream())
                .toList();
    }

    private List<StorageItemDto> getFiles(String prefix, ListObjectsV2Response response) {
        return response.contents()
                .stream()
                .filter(o -> !o.key().equals(prefix))
                .map(o -> mapToStorageItem(o.key(), o.lastModified()))
                .toList();
    }

    private List<StorageItemDto> getDirectories(ListObjectsV2Response response) {
        return response.commonPrefixes()
                .stream()
                .map(CommonPrefix::prefix)
                .map(dir -> mapToStorageItem(dir, null))
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

    private Predicate<StorageItemDto> byModifiedDate(LocalDate from, LocalDate to) {
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

    private void addDirectoryRecursively(
            ZipOutputStream zipOut,
            String dirPath,
            Set<String> addedEntries
    ) {

        addDirectoryEntry(zipOut, dirPath, addedEntries);

        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(dirPath)
                        .build()
        );

        for (S3Object object : response.contents()) {

            if (object.key().equals(dirPath)) {
                continue;
            }

            if (object.key().endsWith("/")) {
                addDirectoryEntry(zipOut, object.key(), addedEntries);
            } else {
                addFile(zipOut, object.key(), object.key(), addedEntries);
            }
        }
    }

    private void addDirectoryEntry(
            ZipOutputStream zipOut,
            String dirPath,
            Set<String> addedEntries
    ) {

        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }

        if (addedEntries.contains(dirPath)) {
            return;
        }

        try {
            zipOut.putNextEntry(new ZipEntry(dirPath));
            zipOut.closeEntry();
            addedEntries.add(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add directory: " + dirPath, e);
        }
    }

    private void addFile(
            ZipOutputStream zipOut,
            String s3Key,
            String zipEntryName,
            Set<String> addedEntries
    ) {

        if (addedEntries.contains(zipEntryName)) {
            return;
        }

        try (
                ResponseInputStream<GetObjectResponse> s3Object =
                        s3Client.getObject(
                                GetObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(s3Key)
                                        .build()
                        )
        ) {
            zipOut.putNextEntry(new ZipEntry(zipEntryName));
            s3Object.transferTo(zipOut);
            zipOut.closeEntry();
            addedEntries.add(zipEntryName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to add file: " + s3Key, e);
        }
    }

    private void addPath(
            ZipOutputStream zipOut,
            String path,
            Set<String> addedEntries
    ) {
        if (path.endsWith("/")) {
            addDirectoryRecursively(zipOut, path, addedEntries);
        } else {
            addFile(zipOut, path, path, addedEntries);
        }
    }

    private void validatePathExists(String path) {
        if (path.endsWith("/")) {
            validateDirectoryExists(path);
        } else {
            validateFileExists(path);
        }
    }

    private void validateFileExists(String path) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(path)
                            .build()
            );
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("File not found: " + path);
        }
    }

    private void validateDirectoryExists(String path) {

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
    }
}
