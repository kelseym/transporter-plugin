package org.nrg.xnatx.plugins.transporter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;

public interface TransporterConfigService {
    String TOOL_ID = "transporter";
    String PATH_MAPPING_ID = "path-mapping";


    void setDefaultPathMapping(String username, String reason) throws JsonProcessingException, ConfigServiceException;

    public TransporterPathMapping getTransporterPathMapping() throws JsonProcessingException;


    void setTransporterPathMapping(String username,
                                   String reason,
                                   TransporterPathMapping pathMapping) throws ConfigServiceException, JsonProcessingException;

    void setTransporterPathMapping(String username, String reason, String xnatRootPath, String serverRootPath) throws ConfigServiceException, JsonProcessingException;
}
