package org.nrg.xnatx.plugins.transporter.config;


import org.mockito.Mockito;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnatx.plugins.transporter.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockConfig {


    @Bean
    NrgPreferenceService mockNrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public SnapshotPreferences mockSnapshotPreferences() {
        return Mockito.mock(SnapshotPreferences.class);
    }

    @Bean
    public DataSnapResolutionService mockDataSnapResolutionService() {
        return Mockito.mock(DataSnapResolutionService.class);
    }

    @Bean
    public DataSnapEntityService mockDataSnapEntityService() {
        return Mockito.mock(DataSnapEntityService.class);
    }

    @Bean
    public SnapUserEntityService mockSnapUserEntityService() {
        return Mockito.mock(SnapUserEntityService.class);
    }

    @Bean
    public TransportActivityService mockTransportActivityService() {
        return Mockito.mock(TransportActivityService.class);
    }

    @Bean
    public TransporterConfigService mockTransporterConfigService() {
        return Mockito.mock(TransporterConfigService.class);
    }

    @Bean
    public PayloadService mockPayloadService() {
        return Mockito.mock(PayloadService.class);
    }

}
