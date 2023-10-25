package org.nrg.xnatx.plugins.transporter.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DataSnapEntityDao extends AbstractHibernateDAO<DataSnapEntity> {

    public DataSnapEntity getByLabel(String name) {
        return findByUniqueProperty("label", name);
    }

    public List<DataSnapEntity> findByOwner(String owner) {
        return findByProperty("owner", owner);
    }
}
