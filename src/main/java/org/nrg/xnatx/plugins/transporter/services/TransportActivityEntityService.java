package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xnatx.plugins.transporter.entities.TransportActivityEntity;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;

import java.util.List;

public interface TransportActivityEntityService {
    TransportActivity create(TransportActivity activity);

    void update(TransportActivity.TransportActivityMessage activityMessage);

    List<TransportActivity> getAllActivity();

    List<TransportActivity> get(String sessionId, String user, String snapshotId);

    TransportActivityEntity fromPojo(TransportActivity activity);

    void delete(String transportSessionId);
}
