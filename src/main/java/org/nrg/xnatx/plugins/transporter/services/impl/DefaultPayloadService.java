package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.PayloadService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DefaultPayloadService implements PayloadService {

    private final DataSnapEntityService dataSnapEntityService;
    private final DataSnapResolutionService dataSnapResolutionService;
    private final TransporterConfigService transporterConfigService;

    @Autowired
    public DefaultPayloadService(final DataSnapEntityService dataSnapEntityService,
                                 final DataSnapResolutionService dataSnapResolutionService,
                                 final TransporterConfigService transporterConfigService) {
        this.dataSnapEntityService = dataSnapEntityService;
        this.dataSnapResolutionService = dataSnapResolutionService;
        this.transporterConfigService = transporterConfigService;
    }

    @Override
    public Payload createPayload(@Nonnull String username, String snapId) throws Exception {
        DataSnap dataSnap = dataSnapEntityService.getDataSnap(username, Long.parseLong(snapId));
        if (dataSnap != null) {
            dataSnap = dataSnapResolutionService.resolveDataSnap(dataSnap);
            return createPayload(dataSnap);
        }
        throw new NotFoundException("No data snap found for user " + username + " with id " + snapId);
    }

    @Override
    public Payload createPayload(@Nonnull DataSnap dataSnap) throws Exception {
        try {
            TransporterPathMapping pathMapping = transporterConfigService.getTransporterPathMapping();
            return Payload.builder().dataSnapId(dataSnap.getId())
                    .name(dataSnap.getLabel())
                    .description(dataSnap.getDescription())
                    .transporterPathMapping(pathMapping)
                    .fileManifests(snapItemsToManifest(dataSnap.streamSnapItems()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new Exception("Failed to retrieve path mapping." + e);
        }
    }

    private List<Payload.FileManifest> snapItemsToManifest(Stream<SnapItem> snapItems) {
        //TODO: Implement conversion from SnapItem to FileManifest
        // Include only 'FILE' type SnapItems

        //return snapItems.map(snapItem -> Payload.FileManifest.builder()
        //        .xnatUri(snapItem.getUri())
        //        .serverPath(snapItem.getPath())
        //        .snapshotPath(snapItem.getSnapshotPath())
        //        .build()).forEach(fileManifest -> log.debug("File manifest: " + fileManifest.toString())
                return null;
    }

//    private Payload.FileManifest snapItemToManifest(SnapItem snapItem) {
//        return Payload.FileManifest.builder()
//                .xnatUri(snapItem.getUri())
//                .serverPath(snapItem.getPath())
//                .snapshotPath(snapItem.getSnapshotPath())
//                .build();
//    }

}
