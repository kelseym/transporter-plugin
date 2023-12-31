package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.PayloadService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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


    // Build a payload object from the DataSnap
    @Override
    public Payload createPayload(@Nonnull DataSnap dataSnap, Payload.Type payloadType) throws Exception {
        return Payload.builder().snapshotId(dataSnap.getId())
                .label(dataSnap.getLabel())
                .description(dataSnap.getDescription())
                .type(payloadType)
                .fileManifests(payloadType.equals(Payload.Type.FILES) ?
                            snapItemsToManifest(dataSnap.streamSnapItems()) :
                            dataSnapToDirectoryManifest(dataSnap))
                .build();
    }

    @Override
    public List<Payload> createPayloads(List<DataSnap> dataSnaps) {
        if (dataSnaps != null) {
            return dataSnaps.stream()
                    .filter(ds -> ds.getBuildState() != null && ds.getBuildState().equals(DataSnap.BuildState.MIRRORED))
                    .map(dataSnap -> {
                        try {
                            return createPayload(dataSnap, Payload.Type.DIRECTORY);
                        } catch (Exception e) {
                            log.error("Error creating payload for data snap " + dataSnap.getLabel(), e);
                            return null;
                        }
            }).collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }
    }

    private List<Payload.FileManifest> dataSnapToDirectoryManifest(DataSnap dataSnap) {
        if (DataSnap.BuildState.MIRRORED.equals(dataSnap.getBuildState())) {
            return Arrays.asList(Payload.FileManifest.builder()
                    .path(dataSnap.getRootPath())
                    .build());
        } else {
            throw new UnsupportedOperationException("Data must be mirrored before it can be transported via directory mount.");
        }
    }

    private List<Payload.FileManifest> snapItemsToManifest(Stream<SnapItem> snapItems) {
        //TODO: Implement conversion from SnapItem to FileManifest
        // Include only 'FILE' type SnapItems
        throw new UnsupportedOperationException("Not yet implemented");
        //return snapItems.map(snapItem -> Payload.FileManifest.builder()
        //        .xnatUri(snapItem.getUri())
        //        .serverPath(snapItem.getPath())
        //        .snapshotPath(snapItem.getSnapshotPath())
        //        .build()).forEach(fileManifest -> log.debug("File manifest: " + fileManifest.toString())

    }


}
