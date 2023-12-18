package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;
import org.nrg.xnatx.plugins.transporter.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.nrg.xnatx.plugins.transporter.model.DataSnap.BuildState.*;
import static org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity.Role.EDITOR;
import static org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity.Role.READER;

@Slf4j
@Service
public class DefaultTransporterService implements TransporterService {

    private final DataSnapResolutionService dataSnapResolutionService;
    private final DataSnapEntityService dataSnapEntityService;
    private final SnapUserEntityService snapUserEntityService;
    private final TransporterActivityService transporterActivityService;
    private final TransporterConfigService transporterConfigService;
    private final PayloadService payloadService;


    @Autowired
    public DefaultTransporterService(final DataSnapResolutionService dataSnapResolutionService,
                                     final DataSnapEntityService dataSnapEntityService,
                                     final SnapUserEntityService snapUserEntityService,
                                     final TransporterActivityService transporterActivityService,
                                     final TransporterConfigService transporterConfigService,
                                     final PayloadService payloadService) {
        this.dataSnapResolutionService = dataSnapResolutionService;
        this.dataSnapEntityService = dataSnapEntityService;
        this.snapUserEntityService = snapUserEntityService;
        this.transporterActivityService = transporterActivityService;
        this.transporterConfigService = transporterConfigService;
        this.payloadService = payloadService;
    }


    @Override
    public List<DataSnap> getDataSnaps(UserI user) {
        return dataSnapEntityService.getDataSnaps(user.getLogin());
    }

    @Override
    public DataSnap getDataSnap(UserI user, String id) throws UnauthorizedException, NotFoundException {
        List<SnapUserEntity> readUsers = snapUserEntityService.findByDataSnapId(Long.parseLong(id));
        if (readUsers.stream().noneMatch(sue -> sue.getLogin().equals(user.getLogin()))) {
            throw new UnauthorizedException("User " + user.getUsername() + " is not authorized to read data snap " + id);
        }
        return dataSnapEntityService.getDataSnap(Long.parseLong(id));
    }

    @Override
    public DataSnap getDataSnapByLabel(UserI user, String label) throws UnauthorizedException, NotFoundException {
        List<SnapUserEntity> readUsers = snapUserEntityService.findByDataSnapLabel(label);
        if (readUsers.stream().noneMatch(sue -> sue.getLogin().equals(user.getLogin()))) {
            throw new UnauthorizedException("User " + user.getUsername() + " is not authorized to read data snap " + label);
        }
        return dataSnapEntityService.getDataSnap(label);
    }

    @Override
    public DataSnap getResolvedDataSnap(UserI user, String id) throws RuntimeException, UnauthorizedException, NotFoundException {
        DataSnap dataSnap = getDataSnap(user, id);
        try {
            if (dataSnap != null) {
                switch (dataSnap.getBuildState()) {
                    case CREATED:
                        DataSnap resolveDataSnap = dataSnapResolutionService.resolveDataSnap(dataSnap);
                        dataSnapEntityService.updateDataSnap(resolveDataSnap);
                    case RESOLVED:
                    case MIRRORED:
                        return dataSnap;
                    default:
                        throw new RuntimeException("Data snap " + dataSnap.getId() + " has an invalid build state: " + dataSnap.getBuildState());
                }
            }
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        return null;
    }

    @Override
    public Optional<DataSnap> storeDataSnap(@Nonnull UserI user, @Nonnull DataSnap dataSnap, Boolean resolve) throws Exception {
        if (resolve) {
            dataSnap = dataSnapResolutionService.resolveDataSnap(dataSnap);
        }
        dataSnap = dataSnapEntityService.createDataSnap(user.getLogin(),dataSnap.toBuilder()
                .buildState(resolve ? RESOLVED : CREATED).build());
        return Optional.of(dataSnap);
    }

    @Override
    public DataSnap addDataSnapEditor(@Nonnull UserI userI, @Nonnull String id, @Nonnull String editorLogin) throws Exception {
        DataSnap dataSnap = dataSnapEntityService.getDataSnapsByOwner(userI.getLogin()).stream()
                .filter(ds -> Objects.equals(ds.getId(), Long.parseLong(id)))
                .findFirst().orElse(null);
        if (dataSnap != null) {
            return dataSnapEntityService.addUser(dataSnap, editorLogin, EDITOR);
        }
        throw new Exception(userI.getLogin() + " can not add user to DataSnap " + id);
    }

    @Override
    public DataSnap addDataSnapReader(@Nonnull UserI userI, @Nonnull String id, @Nonnull String readerLogin) throws Exception {
        DataSnap dataSnap = dataSnapEntityService.getDataSnapsByOwner(userI.getLogin()).stream()
                .filter(ds -> Objects.equals(ds.getId(), Long.parseLong(id)))
                .findFirst().orElse(null);
        if (dataSnap != null) {
            return dataSnapEntityService.addUser(dataSnap, readerLogin, READER);
        }
        throw new Exception(userI.getLogin() + " can not add user to DataSnap " + id);    }

    @Override
    public void removeDataSnapUser(@Nonnull UserI userI, @Nonnull String id) throws Exception {
        DataSnap dataSnap = getDataSnap(userI, id);
        if (dataSnap != null) {
            dataSnapEntityService.removeUser(dataSnap, userI.getLogin());
        }
    }

    @Override
    public void deleteDataSnap(@Nonnull UserI user, @Nonnull String id) throws NotFoundException, UnauthorizedException {
        List<SnapUserEntity> users = snapUserEntityService.findByDataSnapId(Long.parseLong(id));
        if (users.stream().noneMatch(sue -> sue.getLogin().equals(user.getLogin()) && sue.getRole().equals(SnapUserEntity.Role.OWNER))) {
            throw new UnauthorizedException("User " + user.getUsername() + " is not authorized to delete data snap " + id);
        }
        dataSnapEntityService.deleteDataSnap(Long.parseLong(id));
    }


    @Override
    public DataSnap mirrorDataSnap(@Nonnull UserI user, @Nonnull String id, @Nonnull Boolean force) throws Exception {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            switch (dataSnap.getBuildState()) {
                case CREATED:
                    dataSnap = getResolvedDataSnap(user, id);
                case RESOLVED:
                    dataSnap = dataSnapResolutionService.mirrorDataSnap(dataSnap);
                case MIRRORED:
                    if (force) {
                        dataSnap = dataSnapResolutionService.mirrorDataSnap(
                                dataSnapResolutionService.resolveDataSnap(dataSnap));
                    }
                    break;
                default:
                    throw new RuntimeException("Data snap " + dataSnap.getId() + " has an invalid build state: " + dataSnap.getBuildState());
            }
            dataSnapEntityService.updateDataSnap(dataSnap);
        }
        return dataSnap;
    }

    @Override
    public DataSnap getRemappedDataSnap(DataSnap dataSnap) throws RuntimeException {
        try {
            return dataSnapResolutionService.getRemappedDataSnap(dataSnap, transporterConfigService.getTransporterPathMapping());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Payload createPayload(UserI user, String label) throws Exception {
        DataSnap dataSnap = getDataSnapByLabel(user, label);
        dataSnap = mirrorDataSnap(user, Long.toString(dataSnap.getId()), false);
        return payloadService.createPayload(
                transporterConfigService.getTransporterPathMapping().isRemapped()
                ? getRemappedDataSnap(dataSnap) : dataSnap,
                Payload.Type.DIRECTORY);
    }

    @Override
    public List<Payload> getAvailablePayloads(UserI user) {
        List<DataSnap> dataSnaps = getDataSnaps(user);
        return payloadService.createPayloads(dataSnaps);
    }

    @Override
    public void updateRemoteApplicationStatus(RemoteAppHeartbeat heartbeat) {
        transporterActivityService.updateRemoteApplicationStatus(heartbeat);
    }

    @Override
    public List<RemoteAppHeartbeat> getRemoteApplicationStatus() {
        return transporterActivityService.getRemoteApplicationStatus();
    }

    @Override
    public RemoteAppHeartbeat getRemoteApplicationStatus(String remoteAppId) {
        return transporterActivityService.getRemoteApplicationStatus(remoteAppId);
    }

    @Override
    public void updateRemoteApplicationActivity(TransporterActivityItem transporterActivityItem) {
        transporterActivityService.updateRemoteApplicationActivity(transporterActivityItem);
    }

    @Override
    public List<TransporterActivityItem> getRemoteApplicationActivity(UserI user, String snapshotId) {
        return transporterActivityService.getRemoteApplicationActivity(user, snapshotId);
    }


}
