package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class DefaultTransporterService implements TransporterService {

    private DataSnapResolutionService dataSnapResolutionService;
    private DataSnapEntityService dataSnapEntityService;
    private final CatalogService catalogService;


    @Autowired
    public DefaultTransporterService(DataSnapResolutionService dataSnapResolutionService,
                                     DataSnapEntityService dataSnapEntityService,
                                     final CatalogService catalogService) {
        this.dataSnapResolutionService = dataSnapResolutionService;
        this.dataSnapEntityService = dataSnapEntityService;
        this.catalogService = catalogService;
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
    public DataSnap getResolvedDataSnap(UserI user, String id) throws RuntimeException {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            return dataSnapResolutionService.resolveDataSnap(dataSnap);
        }
        return null;
    }

    @Override
    public Boolean storeDataSnap(@Nonnull UserI user, @Nonnull DataSnap dataSnap) {
        return dataSnapEntityService.addDataSnap(user.getLogin(), dataSnap) > 0;
    }


    @Override
    public void deleteDataSnap(@Nonnull UserI user, @Nonnull String id) throws NotFoundException, UnauthorizedException {
        dataSnapEntityService.deleteDataSnap(user.getLogin(), Long.parseLong(id));
    }

    @Override
    public void mirrorDataSnap(@Nonnull UserI user, @Nonnull String id) throws RuntimeException, IOException {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            dataSnapResolutionService.resolveDataSnap(dataSnap);
            dataSnapResolutionService.mirrorDataSnap(dataSnap);
        }
    }



}
