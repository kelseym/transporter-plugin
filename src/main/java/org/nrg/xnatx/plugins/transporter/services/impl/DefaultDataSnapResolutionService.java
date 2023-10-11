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
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
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
import java.util.UUID;

@Slf4j
@Service
public class DefaultDataSnapResolutionService implements DataSnapResolutionService {

    private final static String SNAP_DIR_PREFIX = "snap_";

    private final DataSnapEntityService dataSnapEntityService;
    private final CatalogService catalogService;
    private final SiteConfigPreferences siteConfigPreferences;


    @Autowired
    public DefaultDataSnapResolutionService(final DataSnapEntityService dataSnapEntityService,
                                            final CatalogService catalogService,
                                            final SiteConfigPreferences siteConfigPreferences) {
        this.dataSnapEntityService = dataSnapEntityService;
        this.catalogService = catalogService;
        this.siteConfigPreferences = siteConfigPreferences;
    }

    @Override
    public DataSnap mirrorDataSnap(@Nonnull DataSnap dataSnap) throws RuntimeException, IOException {
        return mirrorDataSnap(dataSnap, getSnapshotDirectory());
    }

    private DataSnap mirrorDataSnap(@Nonnull DataSnap dataSnap, @Nonnull Path targetPath) throws RuntimeException {
        // Iterate over each SnapItem and mirror the file/directory to the target directory
        dataSnap.setMirrorRootPath(targetPath.toString());
        dataSnap.streamSnapItems().forEach(snapItem -> {
            try {
                Path sourcePath = Paths.get(dataSnap.relativeToAbsolutePath(snapItem.getPath()));
                Path destinationPath = targetPath.resolve(dataSnap.absoluteToRelativePath(snapItem.getPath()));
                if (Files.exists(destinationPath)) {
                    throw new IOException("Destination path already exists: " + destinationPath.toString());
                }
                else {
                    Files.createDirectories(destinationPath.getParent());
                    if (Files.isRegularFile(sourcePath)) {
                        Files.createSymbolicLink(destinationPath, sourcePath);
                    }
                }
            } catch (UncheckedIOException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        return dataSnap;
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

    }

    @Override
    public DataSnap resolveDataSnap(DataSnap dataSnap) throws RuntimeException {
        dataSnap.streamSnapItems().forEach(this::resolveHostPath);
        return dataSnap;
    }

    private void resolveHostPath(@Nonnull SnapItem item) throws RuntimeException{
        log.info("Resolving host path for item: " + item.getLabel());
        if (SnapItem.XnatType.RESOURCE.name().equals(item.getXnatType())) {
            try {
                ResourceData resourceData = catalogService
                        .getResourceDataFromUri(item.getUri().replace("/data/","/archive/"), true);
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
                            filter(f -> SnapItem.XnatType.FILE.name().equals(f.getXnatType())).
                            filter(f -> f.getId().equals(relPath)).
                            forEach(f -> f.setPath(filePath));
                }

            } catch (RuntimeException | ClientException | ServerException e) {
                throw new RuntimeException("Could not resolve host path for resource: " + item.getUri(), e.getCause());
            }
        }
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

}
