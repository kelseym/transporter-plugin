package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;

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

    @Nullable
    @JsonProperty("id") private Long id;
    @JsonProperty("label") private String label;
    @Nullable
    @JsonProperty("description") private String description;
    @JsonIgnore
    @JsonProperty("hierarchy-scheme") private HierarchyScheme hierarchyScheme; // PROJECT_SUBJECT || PROJECT_EXPERIMENT || SUBJECT || EXPERIMENT

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
                .hierarchyScheme(
                        request.getHierarchy() != null ? request.getHierarchy() : HierarchyScheme.PROJECT_EXPERIMENT)
                .projects(request.getProjects())
                .dataTypes(request.getDataTypes())
                .resources(request.getResources())
                .build();
    }

    public static String generateLabel(SnapshotRequest request) {
        StringBuilder labelBuilder = new StringBuilder();

        // Append projects to the label
        if (request.getProjects() != null && !request.getProjects().isEmpty()) {
            labelBuilder.append("P-");
            labelBuilder.append(String.join("-", request.getProjects()));
        }
        // Append dataTypes to the label
        if (request.getDataTypes() != null && !request.getDataTypes().isEmpty()) {
            labelBuilder.append("_D-");
            labelBuilder.append(String.join("-", request.getDataTypes()));
        }
        // Append resources to the label
        if (request.getResources() != null && !request.getResources().isEmpty()) {
            labelBuilder.append("_R-");
            labelBuilder.append(String.join("-", request.getResources()));
        }
        return labelBuilder.toString();
    }


    @Getter
    @RequiredArgsConstructor
    public enum HierarchyScheme {
        PROJECT_SUBJECT("Project/Resource|Subject/Resource|Experiment/Resource|Scan/Resource", 1),
        PROJECT_EXPERIMENT("Project/Resource|Experiment/Resource|Scan/Resource", 2),
        SUBJECT("Subject/Resource|Experiment/Resource|Scan/Resource", 3),
        EXPERIMENT("Experiment/Resource|Scan/Resource", 4);

        private final String description;
        private final int code;
    }

    @Data
    public static class SnapshotQuery {
        private UserI userI;
        private List<String> projects;
        private List<String> dataTypes;
        private List<String> resources;

        public SnapshotQuery(UserI userI, SnapshotDefinition snapshotDefinition) {
            this.userI = userI;
            this.projects = snapshotDefinition.getProjects();
            this.dataTypes = snapshotDefinition.getDataTypes();
            this.resources = snapshotDefinition.getResources();
        }
    }

}
