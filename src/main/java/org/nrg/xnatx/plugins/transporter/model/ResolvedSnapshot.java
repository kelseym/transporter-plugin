package org.nrg.xnatx.plugins.transporter.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Slf4j
@ApiModel(value = "ResolvedSnapshot",
        description = "Resolved data manifest structure used to drive the XNAT Transporter function.")
public class ResolvedSnapshot extends Snapshot {

}
