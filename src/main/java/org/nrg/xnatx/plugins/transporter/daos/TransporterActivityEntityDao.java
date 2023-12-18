package org.nrg.xnatx.plugins.transporter.daos;

import com.google.common.base.Strings;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.transporter.entities.TransporterActivityEntity;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransporterActivityEntityDao extends AbstractHibernateDAO<TransporterActivityEntity> {
    public List<TransporterActivityEntity> getByUser(String user) {
        return findByProperty("username", user);
    }

    public List<TransporterActivityEntity> getBySnapshotId(String snapshotId) {
        return findByProperty("snapshotId", snapshotId);
    }

    public List<TransporterActivityEntity> getByUserAndSnapshotId(@Nonnull String user, @Nonnull String snapshotId) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("username", user);
        properties.put("snapshotId", snapshotId);
        return findByProperties(properties);
    }
}
