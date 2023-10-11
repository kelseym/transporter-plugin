package org.nrg.xnatx.plugins.transporter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterPathMapping {

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

}
