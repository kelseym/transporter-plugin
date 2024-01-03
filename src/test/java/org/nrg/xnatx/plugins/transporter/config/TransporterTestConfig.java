package org.nrg.xnatx.plugins.transporter.config;


import org.nrg.framework.services.ContextService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Import({MockConfig.class, HibernateConfig.class})
@TestPropertySource(locations = "classpath:/test.properties")
@ComponentScan({"org.nrg.xnatx.plugins.transporter.services",
        "org.nrg.xnatx.plugins.transporter.services.impl",
        "org.nrg.xnatx.plugins.transporter.daos",
        "org.nrg.xnatx.plugins.transporter.entities"})
public class TransporterTestConfig  {
    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }
}
