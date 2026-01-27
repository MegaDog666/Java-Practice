package com.andreyk.practiceproject.api.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorageObjectDetailsDto {
    String name;
    String path;
    StorageItemType type;
    String lastModified;
    Long size;
}
