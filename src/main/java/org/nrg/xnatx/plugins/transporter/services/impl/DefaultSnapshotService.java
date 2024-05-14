package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotPermissionsException;
import org.nrg.xnatx.plugins.transporter.model.*;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.nrg.xnatx.plugins.transporter.services.SnapshotService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.SnapshotQuery;

import javax.annotation.Nonnull;

@Slf4j
@Service
public class DefaultSnapshotService implements SnapshotService {

    private final static String SNAP_DIR_PREFIX = "snap_";

    private final CatalogService catalogService;
    private final SnapshotPreferences snapshotPreferences;
    private final TransporterConfigService transporterConfigService;

    @Autowired
    public DefaultSnapshotService(final CatalogService catalogService,
                                  final SnapshotPreferences snapshotPreferences,
                                  final TransporterConfigService transporterConfigService) {
        this.catalogService = catalogService;
        this.snapshotPreferences = snapshotPreferences;
        this.transporterConfigService = transporterConfigService;
    }

    @Override
    public ResolvedSnapshot createSnapshot(SnapshotRequest snapshotRequest, Boolean persist) throws Exception {
        SnapshotDefinition snapshotDefinition = SnapshotDefinition.createFromRequest(snapshotRequest);
        return createSnapshot(snapshotDefinition, snapshotRequest.getUser(), persist);
    }

    @Override
    public ResolvedSnapshot createSnapshot(SnapshotDefinition snapshotDefinition, UserI userI, Boolean persist) throws Exception {
        checkReadPermissions(userI, snapshotDefinition.getProjects(), snapshotDefinition.getDataTypes());

        validateSnapshotDefinition(snapshotDefinition, userI);
        // Create snapshot from definition
        // TODO: Should this be stored in a cache to enable rechecking permissions at some interval?
        // Create a new ResolvedSnapshot object
        return resolveSnapshotDefinition(snapshotDefinition, userI);
    }

    @Override
    public void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception {

    }

    @Override
    public MirroredSnapshot mirrorSnapshot(ResolvedSnapshot resolvedSnapshot) throws Exception {
        return mirrorSnapshot(resolvedSnapshot, getNewSnapshotDirectory());
    }


    //** Snapshot Mirroring Methods **//
    private MirroredSnapshot mirrorSnapshot(final ResolvedSnapshot resolvedSnapshot, @Nonnull Path targetPath) throws Exception {
        MirroredSnapshot.MirroredSnapshotBuilder mirroredSnapshotBuilder = MirroredSnapshot.create(resolvedSnapshot).toBuilder();
        for (SnapItem snapItem : resolvedSnapshot.streamSnapItems()) {
            Path sourcePath = Paths.get(snapItem.getPath());
            Path targetItemPath = targetPath.resolve(sourcePath.getFileName());
            try {
                Files.copy(sourcePath, targetItemPath);
            } catch (IOException e) {
                log.error("Could not copy file: " + sourcePath.toString());
                log.error(e.getMessage());
                throw new IOException("Could not copy file", e);
            }
            mirroredSnapshotBuilder.addMirroredItem(MirroredItem.builder()
                    .id(snapItem.getId())
                    .label(snapItem.getLabel())
                    .xnatType(snapItem.getXnatType())
                    .fileType(snapItem.getFileType())
                    .xsiType(snapItem.getXsiType())
                    .path(targetItemPath.toString())
                    .build());
        }
    }

    @Nonnull
    private Path getNewSnapshotDirectory() throws IOException {
        final String rootBuildPath = snapshotPreferences.getSnapshotPath();
        final String uuid = UUID.randomUUID().toString();
        final String buildDir = FilenameUtils.concat(rootBuildPath, SNAP_DIR_PREFIX + uuid);
        final Path created;
        try {
            created = Files.createDirectory(Paths.get(buildDir));
        } catch (IOException e) {
            log.error("Could not create snapshot directory: " + buildDir);
            log.error(e.getMessage());
            throw new IOException("Could not create snapshot directory", e);
        }
        created.toFile().setWritable(true);
        return created;
    }

    private void remapDataSnap(DataSnap dataSnap, TransporterPathMapping transporterPathMapping) {
        String snapRootPath = dataSnap.getRootPath();
        String serverRootPath = transporterPathMapping.getServerRootPath();
        String xnatRootPath = transporterPathMapping.getXnatRootPath();
        // Validate that xnatServerPath is a substring in snapRootPath
        if (!snapRootPath.startsWith(xnatRootPath)) {
            throw new IllegalArgumentException("xnatRootPath is not a substring in snapRootPath. Check for valid path mapping.");
        }
        String remappedRootPath = snapRootPath.replace(Paths.get(xnatRootPath).toString(), Paths.get(serverRootPath).toString());
        dataSnap.setRootPath(remappedRootPath);
    }


    //** Snapshot Resolution Methods **//

    private ResolvedSnapshot resolveSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) {

        // Resolve the snapshot definition into a snapshot query object
        SnapshotQuery snapshotQuery = new SnapshotQuery(userI, snapshotDefinition);
        List<SnapItem> snapItems = new ArrayList<>();

        snapItems.addAll(loadProjectItems(snapshotQuery));

        return ResolvedSnapshot.builder()
                .content(snapItems)
                .build();
    }

    private List<SnapItem> loadProjectItems(SnapshotQuery snapshotQuery) {
        List<SnapItem> snapItems = new ArrayList<>();
        for (String project : snapshotQuery.getProjects()) {
            // Get the project
            XnatProjectdata projectData =
                    XnatProjectdata.getXnatProjectdatasById(project, snapshotQuery.getUserI(), false);
            SnapItem.SnapItemBuilder projectItemBuilder = SnapItem.builder()
                    .id(projectData.getId())
                    .label(projectData.getName())
                    .xnatType(SnapItem.XnatType.PROJECT)
                    .fileType(SnapItem.FileType.DIRECTORY)
                    .xsiType(projectData.getXSIType());
            try {
                projectItemBuilder.path(projectData.getRootArchivePath() + projectData.getCurrentArc());
            } catch (NullPointerException e) {
                log.error("Project could not get root archive path", e);
            }
            // Load project children
            List<SnapItem> projectChildrenItems = loadExperimentItems(projectData, snapshotQuery);
            projectItemBuilder.children(projectChildrenItems.isEmpty() ? null : projectChildrenItems);
            snapItems.add(projectItemBuilder.build());
        }
        return snapItems;
    }

    private List<SnapItem> loadProjectAssetItems(){return null;}
    private List<SnapItem> loadProjectResourceItems(){return null;}
    private List<SnapItem> loadSubjectResourceItems(){return null;}

    private List<SnapItem> loadExperimentItems(XnatProjectdata projectData, SnapshotQuery snapshotQuery) {
        Boolean loadAllDataTypes = snapshotQuery.getDataTypes() == null || snapshotQuery.getDataTypes().isEmpty();
        ArrayList<XnatExperimentdata> experiments = projectData.getExperiments();
        log.debug(loadAllDataTypes ? "All" : snapshotQuery.getDataTypes().toString());
        List<SnapItem> snapItems = new ArrayList<>();
        for (XnatExperimentdata experiment : experiments) {
            if (loadAllDataTypes || snapshotQuery.getDataTypes().contains(experiment.getXSIType())) {
                //TODO: Add cached datatype permissions check
                SnapItem.SnapItemBuilder experimentItemBuilder = SnapItem.builder()
                        .id(experiment.getId())
                        .label(experiment.getLabel())
                        .xnatType(SnapItem.XnatType.EXPERIMENT)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(experiment.getXSIType());
                try {
                    experimentItemBuilder.path(experiment.getCurrentSessionFolder(true));
                } catch (InvalidArchiveStructure | BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
                    log.error("Experiment could not get current session folder", e);}
                // Load experiment children
                List<SnapItem> experimentChildrenItems = loadExperimentResourceItems(experiment, snapshotQuery);
                if (experiment instanceof XnatImagesessiondataI) {
                    experimentChildrenItems.addAll(loadScanItems((XnatImagesessiondataI) experiment, snapshotQuery));
                }
                snapItems.add(
                        experimentItemBuilder
                                .children(experimentChildrenItems.isEmpty() ? null : experimentChildrenItems)
                                .build());
            }
        }
        return snapItems;
    }

    private List<SnapItem> loadExperimentResourceItems(XnatExperimentdata experimentData, SnapshotQuery snapshotQuery) {
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatAbstractresourceI xnatAbstractresourceI : experimentData.getResources_resource()) {
            if (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getXSIType())) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    private List<SnapItem> loadScanItems(XnatImagesessiondataI imageSessionData, SnapshotQuery snapshotQuery) {
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatImagescandataI scanData : imageSessionData.getScans_scan()) {
            SnapItem.SnapItemBuilder scanItemBuilder = SnapItem.builder()
                    .id(scanData.getId())
                    .label(String.format("%s - %s", scanData.getId(), scanData.getType()))
                    .xnatType(SnapItem.XnatType.SCAN)
                    .fileType(SnapItem.FileType.DIRECTORY)
                    .xsiType(scanData.getXSIType());
            if (scanData instanceof XnatImagescandata) {
                scanItemBuilder.path(((XnatImagescandata) scanData).deriveScanDir());
            }
            scanItemBuilder.children(loadScanResourceItems((XnatImagescandata) scanData, snapshotQuery));
            snapItems.add(scanItemBuilder.build());
        }
        return snapItems;
    }

    private List<SnapItem> loadScanResourceItems(XnatImagescandata scanData, SnapshotQuery snapshotQuery) {
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatAbstractresourceI xnatAbstractresourceI : scanData.getFile()) {
            if (xnatAbstractresourceI instanceof XnatResourcecatalog &&
                    (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getXSIType()))) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                try {
                    resourceItemBuilder.path(
                            Paths.get(((XnatResourcecatalog) xnatAbstractresourceI).getFullPath(null))
                            .getParent().toString());
                } catch (Exception e) {log.error("Could not get resource path", e); }
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    private void checkReadPermissions(UserI userI, List<String> projects, List<String> dataTypes) throws SnapshotPermissionsException {
        // Model permissions checks on ContainerServicePermissionsUtil functions
        // Throw permissions exception is user does not have read permissions

    }





}
