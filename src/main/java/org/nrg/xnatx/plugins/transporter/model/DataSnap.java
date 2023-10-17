package org.nrg.xnatx.plugins.transporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import springfox.documentation.spring.web.paths.Paths;

import javax.annotation.Nullable;
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

public class DataSnap {

    @Nullable private Long id;
    private String label;
    @Nullable private String description;
    private String rootPath;
    private List<SnapItem> content;

    @JsonIgnore
    public Stream<SnapItem> streamSnapItems() {
        return content.stream().flatMap(SnapItem::flatten);
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
