package org.nrg.xnatx.plugins.transporter.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
public class MirroredSnapshot extends ResolvedSnapshot {
    static public MirroredSnapshot create(ResolvedSnapshot resolvedSnapshot) {
        MirroredSnapshot mirroredSnapshot = new MirroredSnapshot();
        mirroredSnapshot.setSnapshotDefinition(resolvedSnapshot.getSnapshotDefinition());
        mirroredSnapshot.setCreated(resolvedSnapshot.getCreated());
        mirroredSnapshot.setUpdated(resolvedSnapshot.getUpdated());
        mirroredSnapshot.setRootPath(resolvedSnapshot.getRootPath());
        mirroredSnapshot.setPathRootKey(resolvedSnapshot.getPathRootKey());
        mirroredSnapshot.setBaseType(resolvedSnapshot.getBaseType());
        return mirroredSnapshot;
    }
}
