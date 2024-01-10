package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.transporter.entities.TransportActivityEntity;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityPaginatedRequest;

import java.util.List;

public interface TransportActivityEntityService extends BaseHibernateService<TransportActivityEntity> {
    TransportActivity create(TransportActivity activity);

    void update(TransportActivity.TransportActivityMessage activityMessage);

    List<TransportActivity> getAllActivity();

    List<TransportActivity> get(String sessionId, String user, String snapshotId);

    List<TransportActivity> getPaginated(TransporterActivityPaginatedRequest request);

    TransportActivityEntity fromPojo(TransportActivity activity);

    void delete(String transportSessionId);
}
