package org.nrg.xnatx.plugins.transporter.config;


import org.mockito.Mockito;
import org.nrg.mail.services.MailService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.mockito.Mockito.when;

@Configuration
public class RestApiTestConfig extends WebMvcConfigurerAdapter {
    @Bean
    @Qualifier("mockXnatAppInfo")
    public XnatAppInfo mockAppInfo() {
        XnatAppInfo mockXnatAppInfo = Mockito.mock(XnatAppInfo.class);
        when(mockXnatAppInfo.isPrimaryNode()).thenReturn(true);
        return mockXnatAppInfo;
    }

    @Bean
    public MailService mockMailService() {
        return Mockito.mock(MailService.class);
    }

    @Bean
    @Qualifier("mockRoleService")
    public RoleServiceI mockRoleService() {
        return Mockito.mock(RoleServiceI.class);
    }

    @Bean
    public RoleHolder mockRoleHolder(@Qualifier("mockRoleService") final RoleServiceI roleServiceI,
                                     final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new RoleHolder(roleServiceI, namedParameterJdbcTemplate);
    }

    @Bean
    public NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }

    @Bean
    public UserManagementServiceI mockUserManagementService() {
        return Mockito.mock(UserManagementServiceI.class);
    }

}
