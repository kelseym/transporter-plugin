package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.entities.TransporterActivityEntity;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;

import java.util.List;

public interface TransporterActivityEntityService extends BaseHibernateService<TransporterActivityEntity> {
    TransporterActivityEntity create(TransporterActivityItem activityItem);

    List<TransporterActivityItem> getAllActivity();

    List<TransporterActivityItem> getActivityByUser(UserI user);

    List<TransporterActivityItem> getActivityBySnapshotId(String snapshotId);

    List<TransporterActivityItem> getActivityByUserAndSnapshotId(UserI user, String snapshotId);
}
