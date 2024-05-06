package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotPermissionsException;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.Snapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;
import org.nrg.xnatx.plugins.transporter.model.SnapshotRequest;
import org.nrg.xnatx.plugins.transporter.services.SnapshotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DefaultSnapshotService implements SnapshotService {
    @Override
    public ResolvedSnapshot createSnapshot(SnapshotRequest snapshotRequest, UserI userI, Boolean persist) throws Exception {
        SnapshotDefinition snapshotDefinition = SnapshotDefinition.createFromRequest(snapshotRequest);
        return createSnapshot(snapshotDefinition, userI, persist);
    }

    @Override
    public ResolvedSnapshot createSnapshot(SnapshotDefinition snapshotDefinition, UserI userI, Boolean persist) throws Exception {
        checkReadPermissions(userI, snapshotDefinition.getProjects(), snapshotDefinition.getDataTypes());

        // Create snapshot from definition
        Snapshot snapshot = new Snapshot(snapshotDefinition, userI);

        // Create a new ResolvedSnapshot object

        return null;
    }

    @Override
    public void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception {

    }

    
    private void checkReadPermissions(UserI userI, List<String> projects, List<String> dataTypes) throws SnapshotPermissionsException {
        // Model permissions checks on ContainerServicePermissionsUtil functions
        // Throw permissions exception is user does not have read permissions

    }



}
