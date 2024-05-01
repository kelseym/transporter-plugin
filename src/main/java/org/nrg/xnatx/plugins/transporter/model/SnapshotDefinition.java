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
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "SnapshotDefinition",
        description = "Data manifest structure definition used to drive the XNAT Transporter function.")
@JsonPropertyOrder()
public class SnapshotDefinition implements Serializable {

    @Nullable @JsonProperty("id") private Long id;
    @JsonProperty("label") private String label;
    @Nullable @JsonProperty("description") private String description;
    @JsonProperty("projects") private List<String> projects;
    @JsonProperty("data-types") private List<String> dataTypes;
    @JsonProperty("resources") private List<String> resources;

    public void addProject(String project) {
        projects.add(project);
    }

    public void addDataType(String dataType) {
        dataTypes.add(dataType);
    }

}
