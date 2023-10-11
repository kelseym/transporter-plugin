package org.nrg.xnatx.plugins.transporter.intialization;

import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InitializeTransporter extends AbstractInitializingTask {

    private final TransporterConfigService transporterConfigService;

    @Autowired
    public InitializeTransporter(final TransporterConfigService transporterConfigService) {
        this.transporterConfigService = transporterConfigService;
    }

    @Override
    public String getTaskName() {
        return "Initialize Transporter";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            if (transporterConfigService.getTransporterPathMapping() == null) {
                transporterConfigService.setDefaultPathMapping("admin", "Initializing Transporter");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Transporter.\n" + e.getMessage());
            throw new InitializingTaskException(InitializingTaskException.Level.Error, e.getMessage());
        }
    }
}
