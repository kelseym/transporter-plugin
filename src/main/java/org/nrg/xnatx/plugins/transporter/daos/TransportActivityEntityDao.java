package org.nrg.xnatx.plugins.transporter.daos;

import com.google.common.base.Strings;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.transporter.entities.TransportActivityEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransportActivityEntityDao extends AbstractHibernateDAO<TransportActivityEntity> {

    public TransportActivityEntity getBySessionId(String sessionId) {
        return findByUniqueProperty("sessionId", sessionId);
    }

    public List<TransportActivityEntity> get(String sessionId, String user, String snapshotId) {
        Map<String, Object> properties = new HashMap<>();
        if (!Strings.isNullOrEmpty(sessionId)) {
            properties.put("sessionId", sessionId);
        }
        if (!Strings.isNullOrEmpty(user)) {
            properties.put("username", user);
        }
        if (!Strings.isNullOrEmpty(snapshotId)) {
            properties.put("snapshotId", snapshotId);
        }
        return properties.isEmpty() ? findAll() : findByProperties(properties);
    }

}
