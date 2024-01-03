package org.nrg.xnatx.plugins.transporter.config;

import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class MockConfig {

    @Bean
    public ConfigPaths configPaths() {
        return Mockito.mock(ConfigPaths.class);
    }

    @Bean
    public OrderedProperties orderedProperties() {
        return Mockito.mock(OrderedProperties.class);
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public SearchHelperServiceI mockSearchHelperServiceI() {
        return Mockito.mock(SearchHelperServiceI.class);
    }

    @Bean
    public NrgEventServiceI mockNrgEventService() {
        return Mockito.mock(NrgEventServiceI.class);
    }

    @Bean
    public AliasTokenService mockAliasTokenService() {
        return Mockito.mock(AliasTokenService.class);
    }

    @Bean
    public SiteConfigPreferences mockSiteConfigPreferences() {
        return Mockito.mock(SiteConfigPreferences.class);
    }

    @Bean
    public SnapshotPreferences mockSnapshotPreferences() {
        return Mockito.mock(SnapshotPreferences.class);
    }

    @Bean
    public PermissionsServiceI mockPermissionsService() {
        return Mockito.mock(PermissionsServiceI.class);
    }

    @Bean
    public NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }

    @Bean
    @Qualifier("mockRoleService")
    public RoleServiceI mockRoleService() {
        return Mockito.mock(RoleServiceI.class);
    }

    @Bean
    public RoleHolder mockRoleHolder(@Qualifier("mockRoleService") final RoleServiceI mockRoleService,
                                     final NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate) {
        return new RoleHolder(mockRoleService, mockNamedParameterJdbcTemplate);
    }

    @Bean
    public EventTrackingDataHibernateService mockEventTrackingDataHibernateService() {
        return Mockito.mock(EventTrackingDataHibernateService.class);
    }

    @Bean
    public SerializerService mockSerializerService() {
        return Mockito.mock(SerializerService.class);
    }

    @Bean
    public XnatAppInfo mockXnatAppInfo() {
        return Mockito.mock(XnatAppInfo.class);
    }

}
