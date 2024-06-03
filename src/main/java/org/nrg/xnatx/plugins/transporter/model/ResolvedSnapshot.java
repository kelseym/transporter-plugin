package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "ResolvedSnapshot",
        description = "Resolved data manifest structure used to drive the XNAT Transporter function.")
@JsonPropertyOrder({"definition", "created", "updated", "root-path", "content"})
public class ResolvedSnapshot {
    @JsonProperty("definition") private SnapshotDefinition snapshotDefinition;
    @JsonProperty private Date created;
    @JsonProperty private Date updated;
    @JsonProperty(value = "root-path") private String rootPath;
    @Nullable private List<SnapItem> content;

    public ResolvedSnapshot() {
        this.created = new Date();
        this.updated = new Date();
    }

    public void update() {
        this.updated = new Date();
    }

    public List<String> getDatatypes() {
        List<String> datatypes = snapshotDefinition.getDataTypes();
        datatypes.addAll(snapshotDefinition.getResources());
        return datatypes;
    }

    public List<String> getScanDatatypes() {
        return streamSnapItems(SnapItem.XnatType.SCAN).map(SnapItem::getXsiType).collect(Collectors.toList());
    }

    public List<String> getExperimentsDatatypes() {
        return streamSnapItems(SnapItem.XnatType.EXPERIMENT).map(SnapItem::getXsiType).collect(Collectors.toList());
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
