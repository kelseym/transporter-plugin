package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TransporterService {
    List<DataSnap> getDataSnaps(UserI user);

    DataSnap getDataSnap(UserI user, String id);

    DataSnap getResolvedDataSnap(UserI user, String id);

    Optional<DataSnap> storeDataSnap(UserI user, DataSnap dataSnap);

    void deleteDataSnap(UserI user, String id) throws NotFoundException, UnauthorizedException;

    void deleteDataSnaps(@Nonnull UserI user) throws UnauthorizedException;

    Optional<DataSnap> mirrorDataSnap(UserI user, String id) throws NotFoundException, IOException;
}
