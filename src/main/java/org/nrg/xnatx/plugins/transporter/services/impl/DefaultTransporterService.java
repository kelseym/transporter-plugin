package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.PayloadService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.nrg.xnatx.plugins.transporter.model.DataSnap.BuildState.*;

@Slf4j
@Service
public class DefaultTransporterService implements TransporterService {

    private final DataSnapResolutionService dataSnapResolutionService;
    private final DataSnapEntityService dataSnapEntityService;
    private final CatalogService catalogService;
    private final TransporterConfigService transporterConfigService;
    private final PayloadService payloadService;


    @Autowired
    public DefaultTransporterService(final DataSnapResolutionService dataSnapResolutionService,
                                     final DataSnapEntityService dataSnapEntityService,
                                     final CatalogService catalogService,
                                     final TransporterConfigService transporterConfigService,
                                     final PayloadService payloadService) {
        this.dataSnapResolutionService = dataSnapResolutionService;
        this.dataSnapEntityService = dataSnapEntityService;
        this.catalogService = catalogService;
        this.transporterConfigService = transporterConfigService;
        this.payloadService = payloadService;
    }


    @Override
    public List<DataSnap> getDataSnaps(UserI user) {

        return dataSnapEntityService.getDataSnaps(user.getLogin());
    }

    @Override
    public DataSnap getDataSnap(UserI user, String id) {

        try {
            return dataSnapEntityService.getDataSnap(user.getLogin(), Long.parseLong(id));
        } catch (NotFoundException e) {
            return null;
        }
    }
    @Override
    public DataSnap getDataSnapByLabel(UserI user, String label) {

        try {
            return dataSnapEntityService.getDataSnap(user.getLogin(), label);
        } catch (NotFoundException e) {
            return null;
        }
    }

    @Override
    public DataSnap getResolvedDataSnap(UserI user, String id) throws RuntimeException {
        DataSnap dataSnap = getDataSnap(user, id);
        try {
            if (dataSnap != null) {
                switch (dataSnap.getBuildState()) {
                    case CREATED:
                        DataSnap resolveDataSnap = dataSnapResolutionService.resolveDataSnap(dataSnap);
                        dataSnapEntityService.updateDataSnap(user.getLogin(), resolveDataSnap);
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
    public Optional<DataSnap> storeDataSnap(@Nonnull UserI user, @Nonnull DataSnap dataSnap) {
        return Optional.ofNullable(dataSnapEntityService
                .addDataSnap(user.getLogin(),
                        dataSnap.toBuilder().buildState(CREATED).build()));
    }


    @Override
    public void deleteDataSnap(@Nonnull UserI user, @Nonnull String id) throws NotFoundException, UnauthorizedException {
        dataSnapEntityService.deleteDataSnap(user.getLogin(), Long.parseLong(id));
    }

    // TODO: Is this method needed?
    @Override
    public void deleteDataSnaps(@Nonnull UserI user) throws UnauthorizedException {
        dataSnapEntityService.deleteDataSnaps(user.getLogin());
    }

    @Override
    public DataSnap mirrorDataSnap(@Nonnull UserI user, @Nonnull String id) throws Exception {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            switch (dataSnap.getBuildState()) {
                case CREATED:
                    dataSnap = getResolvedDataSnap(user, id);
                case RESOLVED:
                    dataSnap = dataSnapResolutionService.mirrorDataSnap(dataSnap);
                case MIRRORED:
                    return dataSnap;
                default:
                    throw new RuntimeException("Data snap " + dataSnap.getId() + " has an invalid build state: " + dataSnap.getBuildState());
            }
        }
        return null;
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
        dataSnap = mirrorDataSnap(user, Long.toString(dataSnap.getId()));
        return payloadService.createPayload(
                transporterConfigService.getTransporterPathMapping().isRemapped()
                ? getRemappedDataSnap(dataSnap) : dataSnap,
                Payload.Type.DIRECTORY);
    }

}
