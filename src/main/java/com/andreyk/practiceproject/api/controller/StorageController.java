package com.andreyk.practiceproject.api.controller;

import com.andreyk.practiceproject.api.dto.storage.*;
import com.andreyk.practiceproject.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/objects")
    public List<StorageItemDto> listObjects(
            @RequestParam(required = false, defaultValue = "") String path
    ) {
        return storageService.listObjects(path);
    }

    @GetMapping("/objects/filter")
    public List<StorageItemDto> listObjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate modifiedFrom,
            @RequestParam(required = false) LocalDate modifiedTo
    ) {
        return storageService.listObjects(name, type, modifiedFrom, modifiedTo);
    }

    @PostMapping("/object/details")
    public StorageObjectDetailsDto getDetails(
            @RequestBody StorageObjectDetailsRequest request
    ) {
        return storageService.getObjectDetails(request.getPath());
    }

    @GetMapping("/search")
    public List<StorageItemDto> search(
            @RequestParam String name,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "current") String depth
    ) {
        return storageService.search(name, path, type, depth);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @RequestParam String path
    ) {
        StorageDownloadDto file = storageService.download(path);
        String contentType = file.getContentType();
        MediaType mediaType = MediaType.parseMediaType(
                Optional.of(contentType).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.getSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\""
                )
                .body(file.getResource());
    }

    @PostMapping("/download/zip")
    public ResponseEntity<Resource> downloadZip(
            @RequestBody StorageZipDownloadRequest request
    ) {
        StorageDownloadDto zip = storageService.downloadAsZip(request.getPaths());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"storage_export.zip\""
                )
                .contentLength(zip.getSize())
                .body(zip.getResource());
    }
}
