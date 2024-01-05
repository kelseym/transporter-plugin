package org.nrg.xnatx.plugins.transporter.config;


import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.xnatx.plugins.transporter.services.*;
import org.nrg.xnatx.plugins.transporter.services.impl.DefaultTransporterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({MockConfig.class, HibernateConfig.class, ObjectMapperConfig.class, SerializerConfig.class})
public class TransporterTestConfig {

    @Bean
    public TransporterService transporterService(final DataSnapResolutionService mockDataSnapResolutionService,
                                                 final DataSnapEntityService mockDataSnapEntityService,
                                                 final SnapUserEntityService mockSnapUserEntityService,
                                                 final TransportActivityService mockTransportActivityService,
                                                 final TransporterConfigService mockTransporterConfigService,
                                                 final PayloadService mockPayloadService) {
        return new DefaultTransporterService(mockDataSnapResolutionService,
                mockDataSnapEntityService,
                mockSnapUserEntityService,
                mockTransportActivityService,
                mockTransporterConfigService,
                mockPayloadService);
    }

}
