package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.storage.StorageDownloadDto;
import com.andreyk.practiceproject.api.dto.storage.StorageItemDto;
import com.andreyk.practiceproject.api.dto.storage.StorageObjectDetailsDto;

import java.time.LocalDate;
import java.util.List;

public interface StorageService {
    List<StorageItemDto> listObjects(String path);

    List<StorageItemDto> listObjects(String name, String type, LocalDate modifiedFrom, LocalDate modifiedTo);

    List<StorageItemDto> search(String name, String path, String type, String depth);

    StorageObjectDetailsDto getObjectDetails(String path);

    StorageDownloadDto download(String path);

    StorageDownloadDto downloadAsZip(List<String> paths);
}
