package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.daos.TransporterActivityEntityDao;
import org.nrg.xnatx.plugins.transporter.entities.TransporterActivityEntity;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;
import org.nrg.xnatx.plugins.transporter.services.TransporterActivityEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class DefaultTransporterActivityEntityService extends AbstractHibernateEntityService<TransporterActivityEntity, TransporterActivityEntityDao> implements TransporterActivityEntityService {

    @Override
    public TransporterActivityEntity create(TransporterActivityItem activityItem) {
        return super.create(TransporterActivityEntity.fromPojo(activityItem));
    }

    @Override
    public List<TransporterActivityItem> getAllActivity() {
        return TransporterActivityEntity.toPojo(getAll());
    }

    @Override
    public List<TransporterActivityItem> getActivityByUser(UserI user) {
        return TransporterActivityEntity.toPojo(getDao().getByUser(user.getLogin()));
    }

    @Override
    public List<TransporterActivityItem> getActivityBySnapshotId(String snapshotId) {
        return TransporterActivityEntity.toPojo(getDao().getBySnapshotId(snapshotId));
    }

    @Override
    public List<TransporterActivityItem> getActivityByUserAndSnapshotId(UserI user, String snapshotId) {
        return TransporterActivityEntity.toPojo(getDao().getByUserAndSnapshotId(user.getLogin(), snapshotId));
    }


}
