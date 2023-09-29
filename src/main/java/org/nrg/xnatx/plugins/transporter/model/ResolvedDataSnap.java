package org.nrg.xnatx.plugins.transporter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import io.swagger.annotations.ApiModel;

import java.util.List;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ApiModel(value = "ResolvedDataSnap", description = "Extended DataSnap with host path in each content item.")
public class ResolvedDataSnap extends DataSnap {

    private List<ResolvedSnapItem> content;

}
