package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;

import java.io.IOException;
import java.util.List;

public interface TransporterService {
    List<DataSnap> getDataSnaps(UserI user);

    DataSnap getDataSnap(UserI user, String id);

    DataSnap getResolvedDataSnap(UserI user, String id);

    Boolean storeDataSnap(UserI user, DataSnap dataSnap);

    void deleteDataSnap(UserI user, String id) throws NotFoundException, UnauthorizedException;

    void mirrorDataSnap(UserI user, String id) throws NotFoundException, IOException;
}
