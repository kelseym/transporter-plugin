package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterPathMapping implements Serializable {

    static String DEFAULT_XNAT_ROOT_PATH = "/data/xnat";
    static String DEFAULT_HOST_ROOT_PATH = "/data/xnat";

    private String xnatRootPath;
    private String serverRootPath;

    public static TransporterPathMapping getDefault() {
        return TransporterPathMapping.builder()
                .xnatRootPath(DEFAULT_XNAT_ROOT_PATH)
                .serverRootPath(DEFAULT_HOST_ROOT_PATH)
                .build();
    }

    @JsonIgnore
    public Boolean isRemapped() {
        return Strings.isNullOrEmpty(xnatRootPath) || Strings.isNullOrEmpty(serverRootPath) ?
                false :
                !xnatRootPath.equals(serverRootPath);
    }

}
