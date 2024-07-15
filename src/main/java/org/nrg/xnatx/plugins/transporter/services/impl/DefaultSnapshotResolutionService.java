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

    private final SnapshotPreferences snapshotPreferences;

    @Autowired
    public DefaultSnapshotResolutionService(final SnapshotPreferences snapshotPreferences) {
        this.snapshotPreferences = snapshotPreferences;
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

}
