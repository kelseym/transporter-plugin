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
    private String name;
    private String description;
    private String rootHostPath;
    private List<FileManifest> fileManifests;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileManifest {
        private String xnatUri;
        private String hostpath;
        private String snapshotPath;
    }


}
