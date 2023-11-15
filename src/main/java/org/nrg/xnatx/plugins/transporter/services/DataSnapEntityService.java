package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;

import javax.annotation.Nonnull;
import java.util.List;

public interface DataSnapEntityService {

    DataSnap createDataSnap(String ownerLogin, DataSnap dataSnap);

    DataSnap addUser(DataSnap dataSnap, String userLogin, SnapUserEntity.Role role) throws NotFoundException;

    DataSnap getDataSnap(Long id) throws NotFoundException;

    DataSnap getDataSnap(String label) throws NotFoundException;

    List<DataSnap> getDataSnaps(String userLogin);

    List<DataSnap> getDataSnapsByOwner(String ownerLogin);

    void deleteDataSnap(Long id) throws NotFoundException, UnauthorizedException;

    void updateDataSnap(DataSnap resolveDataSnap) throws NotFoundException;

    void removeUser(DataSnap dataSnap, String userLogin) throws NotFoundException;
}
