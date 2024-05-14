package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.base.Strings;
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
import org.nrg.xnatx.plugins.transporter.model.*;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.nrg.xnatx.plugins.transporter.services.SnapshotResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DefaultSnapshotResolutionService implements SnapshotResolutionService {

    private final static String SNAP_DIR_PREFIX = "snap_";

    private final SnapshotPreferences snapshotPreferences;

    @Autowired
    public DefaultSnapshotResolutionService(final SnapshotPreferences snapshotPreferences) {
        this.snapshotPreferences = snapshotPreferences;
    }

    @Override
    public MirroredSnapshot mirrorSnapshot(ResolvedSnapshot resolvedSnapshot) throws Exception {
        return mirrorSnapshot(resolvedSnapshot, getNewSnapshotDirectory());
    }

    //** Snapshot Mirroring Methods **//
    private MirroredSnapshot mirrorSnapshot(final ResolvedSnapshot resolvedSnapshot, @Nonnull Path targetPath) throws Exception {
        Path originalRootPath = Paths.get(
                Strings.isNullOrEmpty(resolvedSnapshot.getRootPath()) ?
                        Paths.get("/").toString() : resolvedSnapshot.getRootPath());

        // Mirror resource directories
        resolvedSnapshot.streamSnapItems(SnapItem.FileType.DIRECTORY)
                .filter(si -> si.getXnatType().equals(SnapItem.XnatType.RESOURCE)).forEach(snapItem -> {
                    try {
                        Path sourcePath = originalRootPath.resolve(snapItem.getPath());
                        Path destinationPath = targetPath.resolve(snapItem.getPath());
                        if (Files.exists(destinationPath)) {
                            throw new IOException("Destination path already exists: " + destinationPath.toString());
                        } else if (!Files.isDirectory(sourcePath)) {
                            throw new IOException("Source directory path is not a directory type: " + sourcePath.toString());
                        } else {
                            Files.createDirectories(destinationPath.getParent());
                            Files.createSymbolicLink(destinationPath, sourcePath);
                        }
                    } catch (IOException e) {
                        log.error("Could not mirror resource directory: " + snapItem.getPath());
                        log.error(e.getMessage());
                        throw new RuntimeException("Could not mirror resource directory", e);
                    }
                });

        return null;
    }

    @Override
    public ResolvedSnapshot resolveSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) {
        // Resolve the snapshot definition into a snapshot query object
        SnapshotDefinition.SnapshotQuery snapshotQuery = new SnapshotDefinition.SnapshotQuery(userI, snapshotDefinition);

        // Resolve snapshot member item details
        ResolvedSnapshot resolvedSnapshot = ResolvedSnapshot.builder()
                .snapshotDefinition(snapshotDefinition)
                .content(loadProjectItems(snapshotQuery))
                .build();

        // Find the common root path for the snapshot items and transform snapshot to root and relative paths
        List<String> itemPaths =
                resolvedSnapshot.streamSnapItems().map(SnapItem::getPath).collect(Collectors.toList());
        Optional<String> commonRoot = Strings.isNullOrEmpty(snapshotDefinition.getPathRootKey()) ?
                findCommonRoot(itemPaths) :
                findKeyedRoot(findCommonRoot(itemPaths), snapshotDefinition.getPathRootKey());
        try {
            if (commonRoot.isPresent()){
                Path root = Paths.get(commonRoot.get());
                for (SnapItem snapItem: resolvedSnapshot.streamSnapItems()
                        .filter(Objects::nonNull)
                        .filter(si -> !Strings.isNullOrEmpty(si.getPath())).collect(Collectors.toList())) {
                    Path path = Paths.get(snapItem.getPath());
                    Path relPath = root.relativize(path);
                    snapItem.setPath(relPath.toString());
                }
                resolvedSnapshot.setRootPath(commonRoot.get());
            }
        } catch (Throwable e) {
            log.error("Error resolving data snap", e.getMessage());
        }
        return resolvedSnapshot;
    }

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

    private List<SnapItem> loadProjectItems(SnapshotDefinition.SnapshotQuery snapshotQuery) {
        List<SnapItem> snapItems = new ArrayList<>();
        for (XnatProjectdata projectData :
                snapshotQuery.getProjects().stream()
                        .map(project ->
                                XnatProjectdata.getXnatProjectdatasById(project, snapshotQuery.getUserI(), false))
                        .filter(Objects::nonNull).collect(Collectors.toList())) {
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

    private List<SnapItem> loadExperimentItems(XnatProjectdata projectData, SnapshotDefinition.SnapshotQuery snapshotQuery) {
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

    private List<SnapItem> loadExperimentResourceItems(XnatExperimentdata experimentData, SnapshotDefinition.SnapshotQuery snapshotQuery) {
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

    private List<SnapItem> loadScanItems(XnatImagesessiondataI imageSessionData, SnapshotDefinition.SnapshotQuery snapshotQuery) {
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

    private List<SnapItem> loadScanResourceItems(XnatImagescandata scanData, SnapshotDefinition.SnapshotQuery snapshotQuery) {
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


    private static Optional<String> findCommonRoot(List<String> paths) {
        return findCommonRoot(paths.stream());
    }
    private static Optional<String> findCommonRoot(Stream<String> paths) {
        return paths
                .filter(Objects::nonNull)
                .map(Paths::get)
                .reduce(DefaultSnapshotResolutionService::getCommonPath)
                .map(Path::toString);
    }

    private static Path getCommonPath(Path path1, Path path2) {
        int len = Math.min(path1.getNameCount(), path2.getNameCount());
        Path common = path1.getRoot();
        for (int i = 0; i < len; i++) {
            if (!path1.getName(i).equals(path2.getName(i))) {
                return path1.isAbsolute() ?
                        Paths.get(path1.getRoot().toString(), path1.subpath(0, i).toString()) :
                        path1.subpath(0, i);
            }
        }

        return Paths.get(path1.getRoot().toString(), path1.subpath(0, len).toString());
    }

    private static Optional<String> findKeyedRoot(final Optional<String> commonRoot, final String rootKey) {
        if (commonRoot.isPresent() && !Strings.isNullOrEmpty(rootKey)) {
            int keyIndex = commonRoot.get().indexOf(rootKey);
            if (keyIndex == -1) {
                return commonRoot;
            } else {
                return Optional.of(commonRoot.get().substring(0, keyIndex));
            }
        } else {
            return Optional.empty();
        }
    }
}
