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

    public static SnapshotDefinition createFromRequest(SnapshotRequest request) {
        return SnapshotDefinition.builder()
                .label(request.getLabel() != null ?
                        request.getLabel() :
                        generateLabel(request))
                .description(request.getDescription())
                .projects(request.getProjects())
                .dataTypes(request.getDataTypes())
                .resources(request.getResources())
                .build();
    }

    public static String generateLabel(SnapshotRequest request) {
        StringBuilder labelBuilder = new StringBuilder();

        // Append projects to the label
        if (request.getProjects() != null && !request.getProjects().isEmpty()) {
            labelBuilder.append("Projects-");
            labelBuilder.append(String.join("-", request.getProjects()));
        }
        // Append dataTypes to the label
        if (request.getDataTypes() != null && !request.getDataTypes().isEmpty()) {
            labelBuilder.append("_DataTypes-");
            labelBuilder.append(String.join("-", request.getDataTypes()));
        }
        // Append resources to the label
        if (request.getResources() != null && !request.getResources().isEmpty()) {
            labelBuilder.append("_Resources-");
            labelBuilder.append(String.join("-", request.getResources()));
        }
        return labelBuilder.toString();
    }
}
