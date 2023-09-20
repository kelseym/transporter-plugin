/*
 * xnat-template-plugin: org.nrg.xnat.plugins.template.plugin.XnatTemplatePlugin
 * XNAT https://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.transporter;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.*;

import java.util.Arrays;

@XnatPlugin(value = "XnatTransporter", name = "XNAT Transporter Plugin",
            entityPackages = "org.nrg.xnatx.plugins.transporter.model.datasnap.entity")
@ComponentScan({"org.nrg.xnatx.plugins.transporter.rest",
        "org.nrg.xnatx.plugins.transporter.services.impl"})
@Slf4j
public class XnatTransporter {

    public XnatTransporter() {
        //log.error("Creating the XnatTransporter configuration class (logging as ERROR)");
        //log.warn("Creating the XnatTransporter configuration class (logging as WARN)");
        //log.info("Creating the XnatTransporter configuration class (logging as INFO)");
//
        //log.error(log.toString());
        //log.error(this.toString());
        //log.error(XnatTransporter.class.toString());
//
        //log.info(log.toString());
        //log.info(this.toString());
        //log.info(XnatTransporter.class.toString());
//
        //String z = Arrays.toString(Thread.currentThread().getStackTrace());
        //log.error(z);
    }

    @Bean
    public String templatePluginMessage() {
        return "This comes from deep within the XnatTransporter example.";
    }
}
