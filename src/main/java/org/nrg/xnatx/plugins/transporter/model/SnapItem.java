package org.nrg.xnatx.plugins.transporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "label", "file-type", "xnat-type", "xsi-type", "children"})
public class SnapItem implements Serializable {
    @Nullable private String id;
    @Nullable private String label;
    @JsonProperty("file-type") private FileType fileType;
    @Nullable @JsonProperty("xnat-type") private XnatType xnatType;
    @Nullable @JsonProperty("xsi-type") private String xsiType;
    private String uri;
    @Nullable @JsonProperty("path") private String path;
    @Nullable @JsonProperty("relative-path") private String relativePath;
    @Nullable private List<SnapItem> children;

    public enum FileType {
        FILE,
        DIRECTORY
    }

    public enum XnatType {
        PROJECT,
        SUBJECT,
        //SESSION,
        EXPERIMENT,
        SCAN,
        //ASSESSOR,
        RESOURCE,
        FILE
    }

    @JsonIgnore
    public Stream<SnapItem> flatten(XnatType... xnatTypes) {
        return Stream.concat(
                xnatTypes.length == 0 || Arrays.stream(xnatTypes).anyMatch(xnatType -> xnatType.equals(this.xnatType)) ?
                        Stream.of(this) : Stream.empty(),
                children == null ? Stream.empty() : children.stream().flatMap(si-> si.flatten(xnatTypes))
        );
    }

    @JsonIgnore
    public Stream<SnapItem> flatten(FileType fileType) {
        return Stream.concat(
                fileType.equals(this.fileType) ?
                        Stream.of(this) : Stream.empty(),
                children == null ? Stream.empty() : children.stream().flatMap(si-> si.flatten(fileType))
        );
    }
}
