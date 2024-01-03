package org.nrg.xnatx.plugins.transporter.config;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.transporter.rest.TransporterRestApi;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@Import({TransporterTestConfig.class, RestApiTestConfig.class})
public class TransporterRestTestConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public TransporterRestApi transporterRestApi(final TransporterService transporterService,
                                                 final TransporterConfigService mockTransporterConfigService,
                                                 final UserManagementServiceI mockUserManagementService,
                                                 final RoleHolder mockRoleHolder) {
        return new TransporterRestApi(transporterService,
                                      mockTransporterConfigService,
                                      mockUserManagementService,
                                      mockRoleHolder);
    }

    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new TestingAuthenticationProvider());
    }
}
