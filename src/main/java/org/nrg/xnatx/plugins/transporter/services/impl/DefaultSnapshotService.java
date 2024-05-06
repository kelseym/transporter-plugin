package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.exceptions.SnapshotPermissionsException;
import org.nrg.xnatx.plugins.transporter.model.ResolvedSnapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapItem;
import org.nrg.xnatx.plugins.transporter.model.Snapshot;
import org.nrg.xnatx.plugins.transporter.model.SnapshotDefinition;
import org.nrg.xnatx.plugins.transporter.model.SnapshotRequest;
import org.nrg.xnatx.plugins.transporter.services.SnapshotService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultSnapshotService implements SnapshotService {
    @Override
    public ResolvedSnapshot createSnapshot(SnapshotRequest snapshotRequest, UserI userI, Boolean persist) throws Exception {
        SnapshotDefinition snapshotDefinition = SnapshotDefinition.createFromRequest(snapshotRequest);
        return createSnapshot(snapshotDefinition, userI, persist);
    }

    @Override
    public ResolvedSnapshot createSnapshot(SnapshotDefinition snapshotDefinition, UserI userI, Boolean persist) throws Exception {
        checkReadPermissions(userI, snapshotDefinition.getProjects(), snapshotDefinition.getDataTypes());

        validateSnapshotDefinition(snapshotDefinition, userI);
        // Create snapshot from definition
        // TODO: Should this be stored in a cache to enable rechecking permissions at some interval?
        Snapshot snapshot = new Snapshot(snapshotDefinition, userI);

        // Create a new ResolvedSnapshot object

        return null;
    }

    @Override
    public void validateSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) throws Exception {

    }


    private Snapshot resolveSnapshotDefinition(SnapshotDefinition snapshotDefinition, UserI userI) {
        // Resolve the snapshot definition into a snapshot object
        // Start collecting data from the projects and data types
        for (String project : snapshotDefinition.getProjects()) {
            // Get the project
            XnatProjectdata projectData = XnatProjectdata.getXnatProjectdatasById(project, userI, false);
            SnapItem.SnapItemBuilder projectItemBuilder = SnapItem.builder()
                    .id(projectData.getId())
                    .label(projectData.getName())
                    .xnatType(SnapItem.XnatType.PROJECT)
                    .fileType(SnapItem.FileType.DIRECTORY)
                    .xsiType(projectData.getXSIType());
            try {
                projectItemBuilder.path(projectData.getRootArchivePath() + projectData.getCurrentArc();
            } catch (NullPointerException e) {log.error("Project could not get root archive path", e);}

            // Get the data types
            for (String dataType : snapshotDefinition.getDataTypes()) {
                log.debug("Getting experiments by xsi type: " + dataType + " for project: " + projectData.getId());
                ArrayList<XnatSubjectassessordataI> experimentsByXSIType = projectData.getExperimentsByXSIType(dataType);
                log.debug("Found " + experimentsByXSIType.size() + " experiments for xsi type: " + dataType);
                for (XnatSubjectassessordataI experiment : experimentsByXSIType) {
                    SnapItem.SnapItemBuilder experimentItemBuilder = SnapItem.builder()
                            .id(experiment.getId())
                            .label(experiment.getLabel())
                            .xnatType(SnapItem.XnatType.EXPERIMENT)
                            .fileType(SnapItem.FileType.DIRECTORY)
                            .xsiType(experiment.getXSIType());
                    // Get the resources

                }
            }
        }
    }

    private List<SnapItem> loadProjectItems(){return null;}
    private List<SnapItem> loadProjectAssetItems(){return null;}
    private List<SnapItem> loadProjectResourceItems(){return null;}
    private List<SnapItem> loadSubjectResourceItems(){return null;}
    private List<SnapItem> loadExperimentItems(){return null;}
    private List<SnapItem> loadExperimentResourceItems(){return null;}
    private List<SnapItem> loadScanItems(){return null;}
    private List<SnapItem> loadScanResourceItems(){return null;}

    private void checkReadPermissions(UserI userI, List<String> projects, List<String> dataTypes) throws SnapshotPermissionsException {
        // Model permissions checks on ContainerServicePermissionsUtil functions
        // Throw permissions exception is user does not have read permissions

    }





}
