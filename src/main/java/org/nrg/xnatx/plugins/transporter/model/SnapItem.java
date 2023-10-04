package org.nrg.xnatx.plugins.transporter.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SnapItem {
    @Nullable private String id;
    @Nullable private String label;
    @JsonProperty("file-type") private FileType fileType;
    @Nullable @JsonProperty("xnat-type") private String xnatType;
    private String uri;
    @Nullable private String path;
    @Nullable private List<SnapItem> children;

    public enum FileType {
        FILE,
        DIRECTORY
    }

    public enum XnatType {
        PROJECT,
        SUBJECT,
        SESSION,
        SCAN,
        ASSESSOR,
        RESOURCE,
        FILE
    }
}
