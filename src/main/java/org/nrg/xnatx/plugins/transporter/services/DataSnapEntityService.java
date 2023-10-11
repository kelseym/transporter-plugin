package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;

import java.util.List;

public interface DataSnapEntityService {
    Long addDataSnap(String owner, DataSnap dataSnap);

    DataSnap getDataSnap(String owner, Long id) throws NotFoundException;

    List<DataSnap> getDataSnaps(String owner);

    void deleteDataSnap(String login, Long id) throws NotFoundException, UnauthorizedException;
}
