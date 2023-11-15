package org.nrg.xnatx.plugins.transporter.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SnapUserEntityDao extends AbstractHibernateDAO<SnapUserEntity> {


    public List<SnapUserEntity> findByLogin(String login) {
        return findByProperty("login", login);
    }

    public List<SnapUserEntity> findByDataSnapId(Long id) {
        return findByProperty("dataSnapEntity.id", id);
    }

    public List<SnapUserEntity> findByDataSnapLabel(String label) {
        return findByProperty("dataSnapEntity.label", label);
    }
}
