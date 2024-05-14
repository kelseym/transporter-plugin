package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "ResolvedSnapshot",
        description = "Resolved data manifest structure used to drive the XNAT Transporter function.")
public class ResolvedSnapshot {
    @JsonProperty
    private SnapshotDefinition snapshotDefinition;
    @JsonProperty private Date created;
    @JsonProperty private Date updated;
    @JsonProperty(value = "root-path") private String rootPath;
    @Nullable
    @JsonProperty("path-root-key") private String pathRootKey;
    @JsonProperty("base-type") private String baseType;
    @Nullable
    private List<SnapItem> content;

    public ResolvedSnapshot() {
        this.created = new Date();
        this.updated = new Date();
    }

    public void update() {
        this.updated = new Date();
    }

    @JsonIgnore
    public Stream<SnapItem> streamSnapItems(SnapItem.XnatType... xnatTypes) {
        return content.stream().flatMap(si -> si.flatten(xnatTypes));
    }

    @JsonIgnore
    public Stream<SnapItem> streamSnapItems(SnapItem.FileType fileType) {
        return content.stream().flatMap(si -> si.flatten(fileType));
    }

}
