package org.nrg.xnatx.plugins.transporter.model;


import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.List;

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
    private List<SnapItem> content;

}
