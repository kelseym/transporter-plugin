package org.nrg.xnatx.plugins.transporter.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payload {

    private Long id;
    private Long dataSnapId;
    private String label;
    private String description;
    private List<FileManifest> fileManifests;
    private Type type;

    public enum Type {
        FILES,
        DIRECTORY
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileManifest {
        private String xnatUri;
        private String path;
    }

}
