package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.HierarchyScheme;
import org.springframework.util.CollectionUtils;

import static org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.HierarchyScheme.*;

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
    public MirroredSnapshot mirrorSnapshot(final ResolvedSnapshot resolvedSnapshot) throws Exception {
        return mirrorSnapshot(resolvedSnapshot, getNewSnapshotDirectory());
    }

    //** Snapshot Mirroring Methods **//
    private MirroredSnapshot mirrorSnapshot(final ResolvedSnapshot resolvedSnapshot, @Nonnull Path targetPath) throws Exception {

        Map<Integer, Path> relativeSnapshotPathMap = resolveSnapshotTargetHierarchy(
                resolvedSnapshot.getSnapshotDefinition().getHierarchyScheme(), resolvedSnapshot, null);

        // Mirror resource directories of project, subject, and experiment items - depending on the hierarchy scheme

        MirroredSnapshot mirroredSnapshot = MirroredSnapshot.create(resolvedSnapshot, targetPath.toString());
        mirroredSnapshot.streamSnapItems(SnapItem.FileType.DIRECTORY)
                .filter(si -> si.getXnatType().equals(SnapItem.XnatType.RESOURCE))
                .filter(si -> relativeSnapshotPathMap.containsKey(si.hashCode()))
                .forEach(snapItem -> {
                    try {
                        if (snapItem.getPath() == null) {
                            log.error("Resource path for {} is null", snapItem.getLabel());
                            throw new IOException("Resource path is null");
                        }
                        Path sourcePath = Paths.get(snapItem.getPath());
                        Path snapshotRelativePath = relativeSnapshotPathMap.get(snapItem.hashCode());
                        snapItem.setRelativePath(snapshotRelativePath.toString());
                        Path destinationPath = targetPath.resolve(snapshotRelativePath);
                        if (!Files.isDirectory(sourcePath)) {
                            throw new IOException("Source directory path is not a directory type: " + sourcePath.toString());
                        } else
                            // Check if the destination path already exists and is not the same as the target path
                            if (Files.exists(destinationPath)) {
                            throw new IOException("Destination path already exists: " + destinationPath.toString());
                        } else {
                            Files.createDirectories(destinationPath.getParent());
                            Files.createSymbolicLink(destinationPath, sourcePath);
                        }
                    } catch (IOException e) {
                        log.error("Could not mirror resource directory: {}", snapItem.getPath());
                        log.error(e.getMessage());
                        throw new RuntimeException("Could not mirror resource directory", e);
                    }
                });

        return mirroredSnapshot;
    }

    @Override
    public ResolvedSnapshot resolveSnapshotDefinition(SnapshotDefinition snapshotDefinition, final UserI userI) {
        // Resolve the snapshot definition into a snapshot query object
        SnapshotDefinition.SnapshotQuery snapshotQuery = new SnapshotDefinition.SnapshotQuery(userI, snapshotDefinition);

        // Resolve snapshot member item details
        ResolvedSnapshot resolvedSnapshot = ResolvedSnapshot.builder()
                .snapshotDefinition(snapshotDefinition)
                .content(loadProjectItems(snapshotQuery, snapshotDefinition.getHierarchyScheme()))
                .build();

        return resolvedSnapshot;
    }

    private Map<Integer, Path> resolveSnapshotTargetHierarchy(final HierarchyScheme hierarchyScheme,
                                                               final ResolvedSnapshot resolvedSnapshot,
                                                               final Path targetPath) {
        List<SnapItem> rootItems;
        switch (hierarchyScheme) {
            case PROJECT_SUBJECT:
            case PROJECT_EXPERIMENT:
                rootItems = resolvedSnapshot.streamSnapItems(SnapItem.XnatType.PROJECT)
                        .collect(Collectors.toList());
                break;
            case SUBJECT:
                rootItems = resolvedSnapshot.streamSnapItems(SnapItem.XnatType.SUBJECT)
                        .collect(Collectors.toList());
                break;
            case EXPERIMENT:
                rootItems = resolvedSnapshot.streamSnapItems(SnapItem.XnatType.EXPERIMENT)
                        .collect(Collectors.toList());
                break;
            default:
                rootItems = Collections.emptyList();
                log.error("Unimplemented HierarchyScheme found in resolveSnapshotTargetHierarchy: {}", hierarchyScheme);
        }
        return targetPathBuilder(targetPath == null ? Paths.get("") : targetPath, rootItems, hierarchyScheme);
    }

    private Map<Integer, Path> targetPathBuilder(final Path parentPath, final SnapItem snapItem, HierarchyScheme hierarchyScheme) {
        SnapItem.XnatType xnatType = snapItem.getXnatType();
        String nodePath = "";
        switch (xnatType) {
            case PROJECT:
                if (hierarchyScheme.equals(PROJECT_SUBJECT) | hierarchyScheme.equals(HierarchyScheme.PROJECT_EXPERIMENT)) {
                    nodePath = snapItem.getId();
                }
                return targetPathBuilder(parentPath.resolve(nodePath), snapItem.getChildren(), hierarchyScheme);
            case SUBJECT:
                List<SnapItem> childItems;
                if (hierarchyScheme.equals(PROJECT_SUBJECT) | hierarchyScheme.equals(HierarchyScheme.SUBJECT)) {
                    nodePath = snapItem.getId();
                    childItems = snapItem.getChildren();
                } else {
                    // If the hierarchy scheme doesn't include subjects, only pass along experiment children
                    childItems = snapItem.getChildren().stream()
                            .filter(si -> SnapItem.XnatType.EXPERIMENT.equals(si.getXnatType()))
                            .collect(Collectors.toList());
                }
                return targetPathBuilder(parentPath.resolve(nodePath), childItems, hierarchyScheme);
            case RESOURCE:
                // Resource paths are formed from the parent path + resource id
                return Collections.singletonMap(snapItem.hashCode(), parentPath.resolve(snapItem.getLabel()));
            default:
                log.error("Unimplemented XnatType found in targetPathBuilder: " + xnatType);
            case EXPERIMENT:
                return targetPathBuilder(parentPath.resolve(snapItem.getLabel()), snapItem.getChildren(), hierarchyScheme);
            case SCAN:
                return targetPathBuilder(parentPath.resolve(snapItem.getId()), snapItem.getChildren(), hierarchyScheme);
        }
    }

    private Map<Integer, Path> targetPathBuilder(final Path parentPath, final List<SnapItem> snapItems, HierarchyScheme hierarchyScheme) {
        return snapItems == null || snapItems.isEmpty() ? Collections.emptyMap() :
                snapItems.stream()
                        .map(snapItem -> targetPathBuilder(parentPath, snapItem, hierarchyScheme))
                        .flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }



    //private void setCommonRootPath(ResolvedSnapshot resolvedSnapshot, SnapshotDefinition snapshotDefinition) {
//
    //    // Find the common root path for the snapshot items and transform snapshot to root and relative paths
    //    List<String> itemPaths =
    //            resolvedSnapshot.streamSnapItems()
    //                    .filter(Objects::nonNull)
    //                    .map(SnapItem::getPath)
    //                    .distinct()
    //                    .filter(Objects::nonNull)
    //                    .collect(Collectors.toList());
//
    //    Optional<String> commonRoot = Strings.isNullOrEmpty(""snapshotDefinition.getPathRootKey()"") ?
    //            findCommonRoot(itemPaths) :
    //            findKeyedRoot(findCommonRoot(itemPaths), snapshotDefinition.getPathRootKey());
    //    try {
    //        if (commonRoot.isPresent()){
    //            Path root = Paths.get(commonRoot.get());
    //            for (SnapItem snapItem: resolvedSnapshot.streamSnapItems()
    //                    .filter(Objects::nonNull)
    //                    .filter(si -> si.getPath() != null && !Strings.isNullOrEmpty(si.getPath()))
    //                    .collect(Collectors.toList())) {
    //                Path path = Paths.get(snapItem.getPath());
    //                Path relPath = root.relativize(path);
    //                snapItem.setPath(relPath.toString());
    //            }
    //            resolvedSnapshot.setRootPath(commonRoot.get());
    //        }
    //    } catch (Throwable e) {
    //        log.error("Error resolving data snap", e.getMessage());
    //    }
    //}

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

    private List<SnapItem> loadProjectItems(final SnapshotDefinition.SnapshotQuery snapshotQuery, HierarchyScheme hierarchyScheme) {
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
                    .xsiType(projectData.getXSIType());
            try {
                projectItemBuilder.path(projectData.getRootArchivePath() + projectData.getCurrentArc());
            } catch (NullPointerException e) {
                log.error("Project could not get root archive path", e);
            }
            // Load subject resources if the hierarchy scheme includes subjects
            List<SnapItem> projectChildrenItems = new ArrayList<>();
            if (hierarchyScheme.equals(PROJECT_SUBJECT) || hierarchyScheme.equals(SUBJECT)) {
                projectChildrenItems.addAll(loadProjectResourceItems(projectData.getResources_resource(), snapshotQuery));
                // Load subject assessors
                projectChildrenItems.addAll(loadSubjectItems(projectData.getParticipants_participant(), snapshotQuery));
            }
            // Load other experiments - e.g. shared experiments not associated with a subject
            projectChildrenItems.addAll(
                    loadExperimentItems(projectData.getExperiments(), snapshotQuery,
                            projectChildrenItems.stream().flatMap(si -> si.getChildren().stream())
                                    .filter(si -> SnapItem.XnatType.EXPERIMENT.equals(si.getXnatType()))
                                    .map(SnapItem::getId)
                                    .collect(Collectors.toList())));
            projectItemBuilder.children(projectChildrenItems.isEmpty() ? null : projectChildrenItems);
            snapItems.add(projectItemBuilder.build());
        }
        return snapItems;
    }

    private List<SnapItem> loadProjectResourceItems(final List<XnatAbstractresourceI> projectResources, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        List<SnapItem> snapItems = new ArrayList<>();
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        for (final XnatAbstractresourceI xnatAbstractresourceI : projectResources) {
            if (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getLabel())) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                addResourcePath(resourceItemBuilder, xnatAbstractresourceI);
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    // No need to create SnapItem for subjects, just load the subject resource items
    private List<SnapItem> loadSubjectItems(final List<XnatSubjectdata> subjectsData, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        List<SnapItem> subjectSnapItems = new ArrayList<>();
        subjectsData.forEach(subjectData -> {
            SnapItem.SnapItemBuilder subjectItemBuilder = SnapItem.builder()
                    .id(subjectData.getId())
                    .label(subjectData.getLabel())
                    .xnatType(SnapItem.XnatType.SUBJECT)
                    .xsiType(subjectData.getXSIType());
            List<SnapItem> subjectChildItems = loadSubjectResourceItems(subjectData.getResources_resource(), snapshotQuery);
            subjectChildItems.addAll(loadSubjectAssessorItems(subjectData.getExperiments_experiment(), snapshotQuery));
            subjectItemBuilder.children(subjectChildItems);
            subjectSnapItems.add(subjectItemBuilder.build());
        });
        return subjectSnapItems;
    }

    private List<SnapItem> loadSubjectResourceItems(final List<XnatAbstractresourceI> subjectResources, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        List<SnapItem> snapItems = new ArrayList<>();
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        for (final XnatAbstractresourceI xnatAbstractresourceI : subjectResources) {
            if (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getLabel())) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                addResourcePath(resourceItemBuilder, xnatAbstractresourceI);
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    private List<SnapItem> loadSubjectAssessorItems(final List<XnatSubjectassessordataI> subjectAssessors, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        return loadExperimentItems(subjectAssessors.stream()
                .map(sa -> (XnatExperimentdata) sa)
                .collect(Collectors.toList()), snapshotQuery, null);
    }

    private List<SnapItem> loadExperimentItems(final List<XnatExperimentdata> experiments,
                                               final SnapshotDefinition.SnapshotQuery snapshotQuery,
                                               final List<String> excludeIds) {
        Boolean loadAllDataTypes = snapshotQuery.getDataTypes() == null || snapshotQuery.getDataTypes().isEmpty();
        log.debug(loadAllDataTypes ? "All" : snapshotQuery.getDataTypes().toString());
        List<SnapItem> snapItems = new ArrayList<>();
        experiments.stream()
                .filter(experiment -> excludeIds == null || !excludeIds.contains(experiment.getId()))
                .filter(experiment -> loadAllDataTypes || snapshotQuery.getDataTypes().contains(experiment.getXSIType()))
                .forEach(experiment -> {
                    SnapItem.SnapItemBuilder experimentItemBuilder = SnapItem.builder()
                            .id(experiment.getId())
                            .label(experiment.getLabel())
                            .xnatType(SnapItem.XnatType.EXPERIMENT)
                            .xsiType(experiment.getXSIType());
                    List<SnapItem> experimentChildItems = loadExperimentResourceItems(experiment, snapshotQuery);
                    if (experiment instanceof XnatImagesessiondataI) {
                        experimentChildItems.addAll(loadScanItems((XnatImagesessiondataI) experiment, snapshotQuery));
                    }
                    snapItems.add(
                            experimentItemBuilder
                                    .children(experimentChildItems.isEmpty() ? null : experimentChildItems)
                                    .build());
        });
        return snapItems;
    }


    private List<SnapItem> loadExperimentResourceItems(final XnatExperimentdataI experimentData, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatAbstractresourceI xnatAbstractresourceI : experimentData.getResources_resource()) {
            if (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getLabel())) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                addResourcePath(resourceItemBuilder, xnatAbstractresourceI);
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    private List<SnapItem> loadScanItems(final XnatImagesessiondataI imageSessionData, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatImagescandataI scanData : imageSessionData.getScans_scan()) {
            SnapItem.SnapItemBuilder scanItemBuilder = SnapItem.builder()
                    .id(scanData.getId())
                    .label(String.format("%s - %s", scanData.getId(), scanData.getType()))
                    .xnatType(SnapItem.XnatType.SCAN)
                    .xsiType(scanData.getXSIType());
            //if (scanData instanceof XnatImagescandata) {
            //    scanItemBuilder.path(((XnatImagescandata) scanData).deriveScanDir());
            //}
            scanItemBuilder.children(loadScanResourceItems((XnatImagescandata) scanData, snapshotQuery));
            snapItems.add(scanItemBuilder.build());
        }
        return snapItems;
    }

    private List<SnapItem> loadScanResourceItems(final XnatImagescandata scanData, final SnapshotDefinition.SnapshotQuery snapshotQuery) {
        Boolean loadAllResources = snapshotQuery.getResources() == null || snapshotQuery.getResources().isEmpty();
        ArrayList<SnapItem> snapItems = new ArrayList<>();
        for (final XnatAbstractresourceI xnatAbstractresourceI : scanData.getFile()) {
            if (xnatAbstractresourceI instanceof XnatResourcecatalog &&
                    (loadAllResources || snapshotQuery.getResources().contains(xnatAbstractresourceI.getLabel()))) {
                SnapItem.SnapItemBuilder resourceItemBuilder = SnapItem.builder()
                        .id(Integer.toString(xnatAbstractresourceI.getXnatAbstractresourceId()))
                        .label(xnatAbstractresourceI.getLabel())
                        .xnatType(SnapItem.XnatType.RESOURCE)
                        .fileType(SnapItem.FileType.DIRECTORY)
                        .xsiType(xnatAbstractresourceI.getXSIType());
                addResourcePath(resourceItemBuilder, xnatAbstractresourceI);
                snapItems.add(resourceItemBuilder.build());
            }
        }
        return snapItems;
    }

    private void addResourcePath(SnapItem.SnapItemBuilder resourceItemBuilder, final XnatAbstractresourceI xnatAbstractresourceI) {
        try {
            resourceItemBuilder.path(
                    Paths.get(((XnatResourcecatalog) xnatAbstractresourceI).getFullPath(null))
                            .getParent().toString());
        } catch (Exception e) {log.error("Could not get resource path", e); }
    }

    private static Optional<String> findCommonRoot(final List<String> paths) {
        return findCommonRoot(paths.stream());
    }
    private static Optional<String> findCommonRoot(final Stream<String> paths) {
        return paths
                .filter(Objects::nonNull)
                .map(Paths::get)
                .reduce(DefaultSnapshotResolutionService::getCommonPath)
                .map(Path::toString);
    }

    private static Path getCommonPath(final Path path1, final Path path2) {
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
