package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultTransporterService implements TransporterService {

    private DataSnapResolutionService dataSnapResolutionService;
    private DataSnapService dataSnapService;
    private final CatalogService catalogService;


    @Autowired
    public DefaultTransporterService(DataSnapResolutionService dataSnapResolutionService,
                                     DataSnapService dataSnapService,
                                     final CatalogService catalogService) {
        this.dataSnapResolutionService = dataSnapResolutionService;
        this.dataSnapService = dataSnapService;
        this.catalogService = catalogService;
    }


    @Override
    public List<DataSnap> getDataSnaps(UserI user) {

        return dataSnapService.getDataSnaps(user.getLogin());
    }

    @Override
    public DataSnap getDataSnap(UserI user, String id) {

        try {
            return dataSnapService.getDataSnap(user.getLogin(), Long.parseLong(id));
        } catch (NotFoundException e) {
            return null;
        }
    }

    @Override
    public DataSnap getResolvedDataSnap(UserI user, String id) {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            return dataSnapResolutionService.resolveDataSnap(dataSnap);
        }
        return null;
    }

    @Override
    public Boolean storeDataSnap(UserI user, DataSnap dataSnap) {
        return dataSnapService.addDataSnap(user.getLogin(), dataSnap) > 0;
    }





}
