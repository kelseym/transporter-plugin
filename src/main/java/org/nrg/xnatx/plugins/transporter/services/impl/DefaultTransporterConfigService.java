package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultTransporterConfigService implements TransporterConfigService {

    private final ConfigService configService;
    private final ObjectMapper mapper;

    @Autowired
    public DefaultTransporterConfigService(final ConfigService configService,
                                           final ObjectMapper mapper) {
        this.configService = configService;
        this.mapper = mapper;
    }

    @Override
    public void setDefaultPathMapping(final String username, final String reason)
            throws ConfigServiceException, JsonProcessingException {
        setTransporterPathMapping(username, reason, TransporterPathMapping.getDefault());
    }

    @Override
    public TransporterPathMapping getTransporterPathMapping() throws JsonProcessingException {
           final Configuration config = configService.getConfig(TOOL_ID, PATH_MAPPING_ID);
        return config != null ? mapper.readValue(config.getContents(), TransporterPathMapping.class) : null;
    }

    @Override
    public void setTransporterPathMapping(final String username,
                                          final String reason,
                                          final TransporterPathMapping pathMapping)
            throws ConfigServiceException, JsonProcessingException {
        configService.replaceConfig(username, reason, TOOL_ID, PATH_MAPPING_ID,
                mapper.writeValueAsString(pathMapping));
    }

    @Override
    public void setTransporterPathMapping(String username, String reason, String xnatRootPath, String serverRootPath)
            throws ConfigServiceException, JsonProcessingException {
        TransporterPathMapping pathMapping =
                TransporterPathMapping.builder()
                        .xnatRootPath(xnatRootPath)
                        .serverRootPath(serverRootPath).build();
        log.debug("setTransporterPathMapping: pathMapping={}", pathMapping);
        setTransporterPathMapping(username, reason, pathMapping);
    }
}
