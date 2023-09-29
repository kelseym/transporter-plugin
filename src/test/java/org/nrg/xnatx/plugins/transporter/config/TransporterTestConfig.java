package org.nrg.xnatx.plugins.transporter.config;


import org.mockito.Mockito;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = "org.nrg.xnatx.plugins.transporter")
public class TransporterTestConfig {

    @Bean
    public SiteConfigPreferences siteConfigPreferences() {
        return Mockito.mock(SiteConfigPreferences.class);
    }
}
