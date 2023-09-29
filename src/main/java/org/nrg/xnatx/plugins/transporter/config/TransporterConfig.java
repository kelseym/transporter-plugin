/*
 * xnat-template-plugin: org.nrg.xnat.plugins.template.plugin.XnatTemplatePlugin
 * XNAT https://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.transporter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xnat.initialization.RootConfig;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Slf4j
@XnatPlugin(value = "Transporter",
        name = "Transporter",
        description = "XNAT Transporter Plugin",
        logConfigurationFile = "META-INF/resources/transporter-logback.xml",
        entityPackages = "org.nrg.xnatx.plugins.transporter.entities")
@ComponentScan(value = "org.nrg.xnatx.plugins.transporter",
        excludeFilters = @Filter(type = FilterType.REGEX, pattern = ".*TestConfig.*", value = {})
)
@Import({RootConfig.class})
public class TransporterConfig {

    @Bean
    public ObjectMapper objectMapper(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        return objectMapperBuilder.build();
    }

    @Bean
    public String templatePluginMessage() {
        return "XNAT Transporter.";
    }
}
