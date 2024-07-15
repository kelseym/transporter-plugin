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

    private String snapshotId;

    static public MirroredSnapshot create(ResolvedSnapshot resolvedSnapshot, String updatedRootPath) {
        MirroredSnapshot mirroredSnapshot = new MirroredSnapshot();
        mirroredSnapshot.setSnapshotDefinition(resolvedSnapshot.getSnapshotDefinition());
        mirroredSnapshot.setCreated(new Date());
        mirroredSnapshot.setUpdated(new Date());
        mirroredSnapshot.setRootPath(updatedRootPath);
        mirroredSnapshot.setContent(resolvedSnapshot.getContent());
        return mirroredSnapshot;
    }

}
