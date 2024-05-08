package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;

import javax.annotation.Nullable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "Snapshot",
        description = "Data manifest structure used to drive the XNAT Transporter function.")
public class Snapshot {
    @JsonProperty private SnapshotDefinition snapshotDefinition;
    @JsonProperty private Date created;
    @JsonProperty private Date updated;
    @JsonProperty(value = "root-path") private String rootPath;
    @Nullable @JsonProperty("path-root-key") private String pathRootKey;
    @JsonProperty("base-type") private String baseType;

    public Snapshot(SnapshotDefinition snapshotDefinition, UserI userI) {
        this.snapshotDefinition = snapshotDefinition;
    }
}
