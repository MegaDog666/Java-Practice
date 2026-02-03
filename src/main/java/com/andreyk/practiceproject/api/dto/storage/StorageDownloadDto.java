package com.andreyk.practiceproject.api.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorageDownloadDto {
    private String filename;
    private long size;
    private String contentType;
    private Resource resource;
}
