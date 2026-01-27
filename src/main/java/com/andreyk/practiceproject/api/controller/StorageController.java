package com.andreyk.practiceproject.api.controller;

import com.andreyk.practiceproject.api.dto.storage.StorageItemDto;
import com.andreyk.practiceproject.api.dto.storage.StorageObjectDetailsDto;
import com.andreyk.practiceproject.api.dto.storage.StorageObjectDetailsRequest;
import com.andreyk.practiceproject.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
            @RequestParam(required = false) String type
    ) {
        return storageService.search(name, path, type);
    }
}
