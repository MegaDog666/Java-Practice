package com.andreyk.practiceproject.api.dto.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StorageZipDownloadRequest {
    private List<String> paths;
}
