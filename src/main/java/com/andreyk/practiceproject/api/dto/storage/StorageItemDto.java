package com.andreyk.practiceproject.api.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorageItemDto {
    private String name;
    private String path;
    private StorageItemType type;
    private String lastModified;
}
