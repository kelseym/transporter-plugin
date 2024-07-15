package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.nrg.xnatx.plugins.transporter.model.MirroredSnapshot;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.HierarchyScheme;
import org.nrg.xnatx.plugins.transporter.services.SnapshotMirrorService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.HierarchyScheme.PROJECT_SUBJECT;
import static org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition.HierarchyScheme.SUBJECT;

@Slf4j
@Service
public class DefaultSnapshotMirrorService implements SnapshotMirrorService {

    private final static String SNAP_DIR_PREFIX = "snap_";

    private final SnapshotPreferences snapshotPreferences;

    private final Cache<String, MirroredSnapshot> mirroredSnapshotCache;

    @Autowired
    public DefaultSnapshotMirrorService(final SnapshotPreferences snapshotPreferences) {
        this.snapshotPreferences = snapshotPreferences;
         mirroredSnapshotCache = CacheBuilder.newBuilder()
                .expireAfterAccess(snapshotPreferences.getMirrorTimeout(), TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<MirroredSnapshot> retrieveMirroredSnapshots() throws Exception {
        return Collections.emptyList();
    }

    @Override
    public MirroredSnapshot retrieveMirroredSnapshot(String snapshotId) throws Exception {
        return null;
    }

    @Override
    public void deleteMirroredSnapshot(String snapshotId) throws Exception {

    }

    @Override
    public void renewMirroredSnapshotLease(String snapshotId) throws Exception {

    }

    @Override
    public String mirrorAndCacheSnapshot(final ResolvedSnapshot resolvedSnapshot) throws Exception {

        MirroredSnapshot mirroredSnapshot = mirrorSnapshot(resolvedSnapshot);



        return mirroredSnapshot.getSnapshotId();

    }

    //** Snapshot Mirroring Methods **//
    private MirroredSnapshot mirrorSnapshot(final ResolvedSnapshot resolvedSnapshot) throws Exception {

        String mirroredSnapshotId = UUID.randomUUID().toString();
        Path targetPath = getNewSnapshotDirectory(mirroredSnapshotId);

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

        mirroredSnapshot.setSnapshotId(mirroredSnapshotId);

        return mirroredSnapshot;
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


    private Path getSnapshotDirectory(String snapshotId) {
        final String rootBuildPath = snapshotPreferences.getSnapshotPath();
        final String buildDir = FilenameUtils.concat(rootBuildPath, SNAP_DIR_PREFIX + snapshotId);
        return Paths.get(buildDir);
    }

    private Path getNewSnapshotDirectory(String snapshotId) throws IOException {
        final String rootBuildPath = snapshotPreferences.getSnapshotPath();
        final String buildDir = FilenameUtils.concat(rootBuildPath, SNAP_DIR_PREFIX + snapshotId);
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

}
