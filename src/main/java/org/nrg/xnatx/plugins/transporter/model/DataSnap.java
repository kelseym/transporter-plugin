package org.nrg.xnatx.plugins.transporter.model;


import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Super;

import javax.annotation.Nullable;
import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "DataSnap",
        description = "Data manifest structure used to drive the XNAT Transporter function.")

public class DataSnap {

    @Nullable private Long id;
    private String label;
    @Nullable private String description;
    private List<SnapItem> content;

}
