package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotPermissionsException;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotValidationException;
import org.nrg.xnatx.plugins.transporter.model.*;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.nrg.xnatx.plugins.transporter.services.SnapshotResolutionService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DefaultSnapshotService implements SnapshotService {


    private final CatalogService catalogService;
    private final SnapshotPreferences snapshotPreferences;
    private final TransporterConfigService transporterConfigService;

    private final SnapshotResolutionService snapshotResolutionService;

    @Autowired
    public DefaultSnapshotService(final CatalogService catalogService,
                                  final SnapshotPreferences snapshotPreferences,
                                  final TransporterConfigService transporterConfigService,
                                  final SnapshotResolutionService snapshotResolutionService) {
        this.catalogService = catalogService;
        this.snapshotPreferences = snapshotPreferences;
        this.transporterConfigService = transporterConfigService;
        this.snapshotResolutionService = snapshotResolutionService;
    }

    @Override
    public ResolvedSnapshot createSnapshot(SnapshotDefinition snapshotDefinition, UserI userI, Boolean persist) throws Exception {
        checkReadPermissions(userI, snapshotDefinition.getProjects(), snapshotDefinition.getDataTypes());

        validateSnapshotDefinition(snapshotDefinition, userI);
        // Create snapshot from definition
        // TODO: Should this be stored in a cache to enable rechecking permissions at some interval?
        // Create a new ResolvedSnapshot object
        return snapshotResolutionService.resolveSnapshotDefinition(snapshotDefinition, userI);
    }

    @Override
    public void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception {
        Map<Object, Object> validationErrors = Maps.newLinkedHashMap();
        List<String> projects = snapshotDefinition.getProjects();
        if (projects == null || projects.isEmpty()) {
            validationErrors.put("Projects list is empty", "At least one project must be specified");
        }
        if (validationErrors.size() > 0) {
            log.error("Snapshot validation error by " + userI.getLogin());
            throw new SnapshotValidationException("Snapshot validation error by " + userI.getLogin(), validationErrors);
        }
    }

    @Override
    public MirroredSnapshot mirrorSnapshot(ResolvedSnapshot resolvedSnapshot) throws Exception {
        return snapshotResolutionService.mirrorSnapshot(resolvedSnapshot);
    }


    private void checkReadPermissions(UserI userI, List<String> projects, List<String> dataTypes) throws SnapshotPermissionsException {
        // Model permissions checks on ContainerServicePermissionsUtil functions
        // Throw permissions exception is user does not have read permissions

    }





}
