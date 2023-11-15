package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface TransporterService {
    List<DataSnap> getDataSnaps(UserI user);

    DataSnap getDataSnap(UserI user, String id) throws UnauthorizedException, NotFoundException;

    DataSnap getDataSnapByLabel(UserI user, String label) throws UnauthorizedException, NotFoundException;

    DataSnap getResolvedDataSnap(UserI user, String id) throws UnauthorizedException, NotFoundException;

    Optional<DataSnap> storeDataSnap(UserI user, DataSnap dataSnap, Boolean resolve) throws Exception;

    DataSnap addDataSnapEditor(@Nonnull UserI userI, @Nonnull String id, @Nonnull String editorLogin) throws Exception;

    DataSnap addDataSnapReader(@Nonnull UserI userI, @Nonnull String id, @Nonnull String readerLogin) throws Exception;

    void removeDataSnapUser(@Nonnull UserI userI, @Nonnull String id) throws Exception;

    void deleteDataSnap(UserI user, String id) throws NotFoundException, UnauthorizedException;

    DataSnap mirrorDataSnap(UserI user, String id, Boolean force) throws Exception;

    DataSnap getRemappedDataSnap(DataSnap dataSnap) throws RuntimeException;

    Payload createPayload(UserI user, String label) throws Exception;

    List<Payload> getAvailablePayloads(UserI user);
}
