package org.nrg.xnatx.plugins.transporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SnapItem {
    @Nullable private String id;
    @Nullable private String label;
    @JsonProperty("file-type") private FileType fileType;
    @Nullable @JsonProperty("xnat-type") private XnatType xnatType;
    private String uri;
    @Nullable @JsonProperty("path") private String path;
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

    @JsonIgnore
    public Stream<SnapItem> flatten(XnatType... xnatTypes) {
        return Stream.concat(
                xnatTypes.length == 0 || Arrays.stream(xnatTypes).anyMatch(xnatType -> xnatType.equals(this.xnatType)) ?
                        Stream.of(this) : Stream.empty(),
                children == null ? Stream.empty() : children.stream().flatMap(si-> si.flatten(xnatTypes))
        );
    }

}
