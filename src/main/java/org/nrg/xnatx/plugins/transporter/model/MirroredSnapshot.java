package org.nrg.xnatx.plugins.transporter.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MirroredSnapshot extends ResolvedSnapshot {
    static public MirroredSnapshot create(ResolvedSnapshot resolvedSnapshot) {
        MirroredSnapshot mirroredSnapshot = new MirroredSnapshot();
        mirroredSnapshot.setSnapshotDefinition(resolvedSnapshot.getSnapshotDefinition());
        mirroredSnapshot.setCreated(new Date());
        mirroredSnapshot.setUpdated(new Date());
        mirroredSnapshot.setRootPath(resolvedSnapshot.getRootPath());
        return mirroredSnapshot;
    }
}
