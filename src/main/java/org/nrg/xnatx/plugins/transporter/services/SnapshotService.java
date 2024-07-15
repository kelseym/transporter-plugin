package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.MirroredSnapshot;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;
import org.nrg.xnatx.plugins.transporter.model.SnapshotRequest;

public interface SnapshotService {

    ResolvedSnapshot createSnapshot(SnapshotDefinition snapshotDefinition, UserI userI, Boolean persist) throws Exception;

    void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception;

    String mirrorSnapshot(ResolvedSnapshot resolvedSnapshot) throws Exception;
}
