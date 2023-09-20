package org.nrg.xnatx.plugins.transporter.model;


import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "DataSnap", description = "Basic data directory description provided to drive XNAT Transporter function.")
public class DataSnap {

    private Long id;
    private String name;
    private String description;
    private String path;

}
