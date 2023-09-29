package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.ResolvedDataSnap;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapItem;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.services.DataSnapService;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultTransporterService implements TransporterService {

    private DataSnapService dataSnapService;
    private final CatalogService catalogService;


    @Autowired
    public DefaultTransporterService(DataSnapService dataSnapService,
                                     final CatalogService catalogService) {
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
    public ResolvedDataSnap getResolvedDataSnap(UserI user, String id) {
        DataSnap dataSnap = getDataSnap(user, id);
        if (dataSnap != null) {
            return resolveDataSnap(dataSnap);
        }
        return null;
    }

    @Override
    public Boolean storeDataSnap(UserI user, DataSnap dataSnap) {
        return dataSnapService.addDataSnap(user.getLogin(), dataSnap) > 0;
    }


    @Override
    public ResolvedDataSnap resolveDataSnap(DataSnap dataSnap) {
        // Resolve absolute paths based on DataSnap URI property
        List<ResolvedSnapItem> resolvedSnapItems = dataSnap.getContent().stream()
                .map(snapItem -> resolveSnapItem(snapItem))
                .collect(Collectors.toList());

        return ResolvedDataSnap.builder()
                .id(dataSnap.getId())
                .label(dataSnap.getLabel())
                .description(dataSnap.getDescription())
                .content(resolvedSnapItems)
                .build();
    }

    private ResolvedSnapItem resolveSnapItem(SnapItem item) {
        return ResolvedSnapItem.builder()
                .uri(item.getUri())
                .fileType(item.getFileType())
                .path(resolveHostPath(item))
                .build();
    }

    private String resolveHostPath(SnapItem item) {
        try {
            return catalogService
                    .getResourceDataFromUri(item.getUri(), item.getFileType() == SnapItem.FileType.FILE ? true : false)
                    .getResourceFilePath();

        } catch (ClientException e) {
            log.error("Error resolving host path for item: " + item.getUri(), e);
        }
        return null;
    }

}
