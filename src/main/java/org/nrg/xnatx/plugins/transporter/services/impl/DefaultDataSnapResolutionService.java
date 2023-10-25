package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.nrg.xnatx.plugins.transporter.model.DataSnap.BuildState.MIRRORED;
import static org.nrg.xnatx.plugins.transporter.model.DataSnap.BuildState.RESOLVED;

@Slf4j
@Service
public class DefaultDataSnapResolutionService implements DataSnapResolutionService {

    private final static String SNAP_DIR_PREFIX = "snap_";

    private final DataSnapEntityService dataSnapEntityService;
    private final CatalogService catalogService;
    private final SiteConfigPreferences siteConfigPreferences;
    private final TransporterConfigService transporterConfigService;


    @Autowired
    public DefaultDataSnapResolutionService(final DataSnapEntityService dataSnapEntityService,
                                            final CatalogService catalogService,
                                            final SiteConfigPreferences siteConfigPreferences,
                                            final TransporterConfigService transporterConfigService) {
        this.dataSnapEntityService = dataSnapEntityService;
        this.catalogService = catalogService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.transporterConfigService = transporterConfigService;
    }

    @Override
    public DataSnap mirrorDataSnap(@Nonnull DataSnap dataSnap) throws Exception {
        return mirrorDataSnap(dataSnap, getSnapshotDirectory());
    }

    private DataSnap mirrorDataSnap(@Nonnull DataSnap dataSnap, @Nonnull Path targetPath) throws Exception {
        // Iterate over each SnapItem and mirror the file/directory to the target directory
        Path originalRootPath = Paths.get(
                Strings.isNullOrEmpty(dataSnap.getRootPath()) ?
                        Paths.get("/").toString() : dataSnap.getRootPath());
        TransporterPathMapping transporterPathMapping = transporterConfigService.getTransporterPathMapping();
        dataSnap.streamSnapItems(SnapItem.XnatType.FILE).forEach(snapItem -> {
            try {
                Path sourcePath = originalRootPath.resolve(snapItem.getPath());
                Path destinationPath = targetPath.resolve(snapItem.getPath());
                if (Files.exists(destinationPath)) {
                    throw new IOException("Destination path already exists: " + destinationPath.toString());
                }
                else {
                    Files.createDirectories(destinationPath.getParent());
                    if (Files.isRegularFile(sourcePath)) {
                        Files.createSymbolicLink(remapRootDirectory(transporterPathMapping, destinationPath), sourcePath);
                    }
                }
            } catch (UncheckedIOException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataSnap.setRootPath(targetPath.toString());
        dataSnap.setBuildState(MIRRORED);
        return dataSnap;
    }

    private Path remapRootDirectory(TransporterPathMapping transporterPathMapping, Path originalPath) {
        if (!transporterPathMapping.isRemapped())
            return originalPath;
        Path originalRoot = Paths.get(transporterPathMapping.getXnatRootPath());
        Path relativePath = originalRoot.relativize(originalPath);
        return Paths.get(transporterPathMapping.getServerRootPath()).resolve(relativePath);
    }

    @Override
    public void validateDataSnap(DataSnap dataSnap, Boolean expectResolved) throws ValidationException {
        Map<String, Object> errors = Maps.newLinkedHashMap();
        if (dataSnap == null) {
            errors.put("dataSnap", "DataSnap cannot be null");
        }
        else {
            if (StringUtils.isBlank(dataSnap.getLabel())) {
                errors.put("label", "Label cannot be blank");
            }
            if (expectResolved && Strings.isNullOrEmpty(dataSnap.getRootPath())) {
                errors.put("resolved", "DataSnap is missing root path.");
            }
            if (dataSnap.streamSnapItems().count() == 0) {
                errors.put("snapItems", "DataSnap must contain at least one SnapItem");
            } else {
                dataSnap.streamSnapItems().forEach(snapItem -> validateSnapItem(snapItem, expectResolved, errors));
            }
        }
    }

    private void validateSnapItem(SnapItem snapItem, Boolean expectResolved, Map errors) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DataSnap resolveDataSnap(DataSnap dataSnap) throws RuntimeException {
        dataSnap.streamSnapItems().forEach(this::resolveHostPath);
        Optional<String> rootPath = findCommonRoot(dataSnap.streamSnapItems().map(SnapItem::getPath));
        try {
            if (rootPath.isPresent()){
                dataSnap.setRootPath(rootPath.get());
                Path root = Paths.get(rootPath.get());
                // TODO: Why doesn't the "stream version" work?
                //dataSnap.streamSnapItems()
                //        .filter(Objects::nonNull)
                //        .filter(si -> !Strings.isNullOrEmpty(si.getPath()))
                //        .forEach(si -> si.setPath(root.relativize(Paths.get(si.getPath())).toString()));
                for (SnapItem snapItem: dataSnap.streamSnapItems()
                        .filter(Objects::nonNull)
                        .filter(si -> !Strings.isNullOrEmpty(si.getPath())).collect(Collectors.toList())) {
                    Path path = Paths.get(snapItem.getPath());
                    Path relPath = root.relativize(path);
                    snapItem.setPath(relPath.toString());
                }
                dataSnap.setRootPath(rootPath.get());
            }
        } catch (Throwable e) {
            log.error("Error resolving data snap", e.getMessage());
        }
        dataSnap.setBuildState(RESOLVED);
        return dataSnap;
    }

    @Override
    public DataSnap getRemappedDataSnap(DataSnap dataSnap, TransporterPathMapping transporterPathMapping) {
        if (dataSnap.getBuildState().equals(MIRRORED) ||
                dataSnap.getBuildState().equals(RESOLVED)) {
            remapDataSnap(dataSnap, transporterPathMapping);
            return dataSnap;
        } else {
            throw new RuntimeException("DataSnap must be in MIRRORED or RESOLVED state to remap.");
        }
    }

    private void resolveHostPath(@Nonnull SnapItem item) throws RuntimeException{
        log.info("Resolving host path for item: " + item.getLabel());
        if (SnapItem.XnatType.RESOURCE.equals(item.getXnatType())) {
            try {
                ResourceData resourceData = catalogService
                        .getResourceDataFromUri(item.getUri()
                                .replace("/data/","/archive/").
                                replace("/archive/archive/", "/archive/"), // TODO: Fix this hack
                                true);
                final URIManager.ArchiveItemURI resourceUri = resourceData.getXnatUri();
                final XnatAbstractresourceI xnatAbstractresourceI = ((ResourceURII)resourceUri).getXnatResource();
                final XnatResourcecatalog xnatResourcecatalog = (XnatResourcecatalog) xnatAbstractresourceI;
                File catalogFile = CatalogUtils.getOrCreateCatalogFile(null, xnatResourcecatalog, null);
                if(catalogFile == null){
                    throw new RuntimeException("Could not find catalog file for resource: " + item.getUri());
                }
                item.setPath(catalogFile.getParent());
                final CatCatalogBean cat = CatalogUtils.getCatalog(null, xnatResourcecatalog, null);
                final Path parentPath = Paths.get(item.getUri() + "/files/");
                final List<Object[]> entryDetails = CatalogUtils.getEntryDetails(cat, item.getPath(), parentPath.toString(),
                        xnatResourcecatalog, false, null, null, "URI");
                for (final Object[] entry : entryDetails) {
                    String uri = (String) entry[2]; // This is the parentUri + relative path to file
                    String relPath = parentPath.relativize(Paths.get(uri)).toString(); // get that relative path
                    String filePath = Paths.get(item.getPath()).resolve(relPath).toString(); // append rel path to parent dir
                    String tagsCsv = (String) entry[4];
                    String format = (String) entry[5];
                    String content = (String) entry[5];
                    String sizeStr = StringUtils.defaultIfBlank((String) entry[1], null);
                    Long size = sizeStr == null ? null : Long.parseLong(sizeStr);
                    String checksum = (String) entry[8];
                    item.getChildren().stream().
                            filter(f -> SnapItem.XnatType.FILE.equals(f.getXnatType())).
                            filter(f -> f.getId().equals(relPath)).
                            forEach(f -> f.setPath(filePath));
                }

            } catch (RuntimeException | ClientException | ServerException e) {
                throw new RuntimeException("Could not resolve host path for resource: " + item.getUri(), e.getCause());
            }
        }
    }

    private static Optional<String> findCommonRoot(Stream<String> paths) {
        return paths
                .filter(Objects::nonNull)
                .map(Paths::get)
                .reduce(DefaultDataSnapResolutionService::getCommonPath)
                .map(Path::toString);
    }

    private static Path getCommonPath(Path path1, Path path2) {
        int len = Math.min(path1.getNameCount(), path2.getNameCount());
        Path common = path1.getRoot();
        for (int i = 0; i < len; i++) {
            if (!path1.getName(i).equals(path2.getName(i))) {
                return path1.subpath(0, i);
            }
        }

        return Paths.get(path1.getRoot().toString(), path1.subpath(0, len).toString());
    }

    // TODO: Define a unique directory (in xdat) to store snapshots, e.g. /data/xnat/snapshots
    @Nonnull
    private Path getSnapshotDirectory() throws IOException {
        final String rootBuildPath = siteConfigPreferences.getBuildPath();
        final String uuid = UUID.randomUUID().toString();
        final String buildDir = FilenameUtils.concat(rootBuildPath, SNAP_DIR_PREFIX + uuid);
        final Path created;
        try {
            created = Files.createDirectory(Paths.get(buildDir));
        } catch (IOException e) {
            throw new IOException("Could not create build directory", e);
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

}
