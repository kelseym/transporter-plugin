package org.nrg.xnatx.plugins.transporter.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;
import org.nrg.xnatx.plugins.transporter.model.SnapshotRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultSnapshotService implements SnapshotService {
    @Override
    public ResolvedSnapshot createSnapshot(SnapshotRequest snapshotRequest, UserI userI, Boolean persist) throws Exception {
        return null;
    }

    @Override
    public void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception {

    }
}
