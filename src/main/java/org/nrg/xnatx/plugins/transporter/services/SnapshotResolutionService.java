package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.MirroredSnapshot;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;

public interface SnapshotResolutionService {
    public ResolvedSnapshot resolveSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI);

}
