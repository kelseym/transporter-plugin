package org.nrg.xnatx.plugins.transporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "DataSnap",
        description = "Data manifest structure used to drive the XNAT Transporter function.")

@JsonPropertyOrder({"id", "label", "description", "root-path", "path-root-key", "base-type", "build-state", "content"})
public class DataSnap implements Serializable {

    @Nullable
    private Long id;
    private String label;
    @Nullable
    private String description;
    @JsonProperty(value = "root-path")
    private String rootPath;
    @Nullable
    @JsonProperty("path-root-key")
    private String pathRootKey;
    @JsonProperty("base-type")
    private String baseType;
    @JsonProperty("build-state")
    private BuildState buildState;
    @Nullable
    private List<SnapItem> content;

    public enum BuildState {
        CREATED,
        RESOLVED,
        MIRRORED,
        FAILED
    }

    @JsonIgnore
    public Stream<SnapItem> streamSnapItems(SnapItem.XnatType... xnatTypes) {
        return content.stream().flatMap(si -> si.flatten(xnatTypes));
    }

    @JsonIgnore
    public Stream<SnapItem> streamSnapItems(SnapItem.FileType fileType) {
        return content.stream().flatMap(si -> si.flatten(fileType));
    }

    @JsonIgnore
    public String absoluteToRelativePath(String absolutePath) {
        return absolutePath.startsWith(rootPath) ? absolutePath : absolutePath.replaceFirst(rootPath, "");
    }

    @JsonIgnore
    public String relativeToAbsolutePath(String relativePath) {
        return FilenameUtils.concat(rootPath, relativePath);
    }

}
