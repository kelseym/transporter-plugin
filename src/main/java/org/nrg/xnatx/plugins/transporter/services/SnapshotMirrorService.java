package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.MirroredSnapshot;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;

import java.util.List;

public interface SnapshotMirrorService {

    public List<MirroredSnapshot> retrieveMirroredSnapshots() throws Exception;

    public MirroredSnapshot retrieveMirroredSnapshot(String snapshotId) throws Exception;

    public void deleteMirroredSnapshot(String snapshotId) throws Exception;

    public void renewMirroredSnapshotLease(String snapshotId) throws Exception;

    public String mirrorAndCacheSnapshot(ResolvedSnapshot resolvedSnapshot) throws Exception;

}
