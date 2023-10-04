package org.nrg.xnatx.plugins.transporter.services.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class DefaultDataSnapResolutionService implements DataSnapResolutionService {

    private DataSnapService dataSnapService;
    private final CatalogService catalogService;

    @Autowired
    public DefaultDataSnapResolutionService(DataSnapService dataSnapService,
                                            final CatalogService catalogService) {
        this.dataSnapService = dataSnapService;
        this.catalogService = catalogService;
    }


    @Override
    public DataSnap resolveDataSnap(DataSnap dataSnap) {
        dataSnap.getContent().stream().forEach(this::resolveSnapItem);
        return dataSnap;
    }

    private void resolveSnapItem(SnapItem item) {
        resolveHostPath(item);
        if (item.getChildren() != null) {
            item.getChildren().stream().forEach(this::resolveSnapItem);
        }
    }

    private void resolveHostPath(SnapItem item) {
        if (SnapItem.XnatType.RESOURCE.name().equals(item.getXnatType())) {
            try {
                ResourceData resourceData = catalogService
                        .getResourceDataFromUri(item.getUri().replace("/data/","/archive/"), true);
                final URIManager.ArchiveItemURI resourceUri = resourceData.getXnatUri();
                final XnatAbstractresourceI xnatAbstractresourceI = ((ResourceURII)resourceUri).getXnatResource();
                final XnatResourcecatalog xnatResourcecatalog = (XnatResourcecatalog) xnatAbstractresourceI;
                item.setPath(CatalogUtils.getOrCreateCatalogFile(null, xnatResourcecatalog, null).getParent());
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

            } catch (ClientException | ServerException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
