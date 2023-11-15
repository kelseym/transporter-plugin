package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;

import java.util.List;

import static org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity.Role;
public interface SnapUserEntityService {

    SnapUserEntity create(SnapUserEntity entity);
    List<DataSnapEntity> getDataSnaps(String login);

    List<DataSnapEntity> getDataSnapsByOwner(String login);

    List<SnapUserEntity> findByDataSnapId(Long id);

    List<SnapUserEntity> findByDataSnapLabel(String label);
}
