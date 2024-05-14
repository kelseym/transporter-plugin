package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.nrg.xft.security.UserI;

import javax.annotation.Nullable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@ApiModel(value = "SnapshotRequest",
        description = "Client request for snapshot data.")
@JsonPropertyOrder({"label", "description", "path-root-key", "projects", "data-types", "resources"})
public class SnapshotRequest {
    @Nullable @JsonProperty("label") private String label;
    @Nullable @JsonProperty("description") private String description;
    @Nullable @JsonProperty("path-root-key") private String pathRootKey;
    @Nullable @JsonProperty("projects") private List<String> projects;
    @Nullable @JsonProperty("data-types") private List<String> dataTypes;
    @Nullable @JsonProperty("resources") private List<String> resources;
}
