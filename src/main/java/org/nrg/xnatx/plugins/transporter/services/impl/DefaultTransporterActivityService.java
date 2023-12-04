package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;
import org.nrg.xnatx.plugins.transporter.services.TransporterActivityEntityService;
import org.nrg.xnatx.plugins.transporter.services.TransporterActivityService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultTransporterActivityService implements TransporterActivityService {

    // In-memory map of active remote applications
    private Map<String, RemoteAppHeartbeat> remoteApplicationStatusMap = new HashMap<>();

    private final TransporterActivityEntityService transporterActivityEntityService;

    public DefaultTransporterActivityService(TransporterActivityEntityService transporterActivityEntityService) {
        this.transporterActivityEntityService = transporterActivityEntityService;
    }

    @Override
    public void updateRemoteApplicationStatus(RemoteAppHeartbeat heartbeat) {
        remoteApplicationStatusMap.put(heartbeat.getRemoteAppId(), heartbeat);
    }

    @Override
    public List<RemoteAppHeartbeat> getRemoteApplicationStatus() {
        return remoteApplicationStatusMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public RemoteAppHeartbeat getRemoteApplicationStatus(String remoteAppId) {
        return remoteApplicationStatusMap.get(remoteAppId);
    }

    @Override
    public void updateRemoteApplicationActivity(TransporterActivityItem activityItem) {
        transporterActivityEntityService.create(activityItem);
    }

    @Override
    public List<TransporterActivityItem> getRemoteApplicationActivity(UserI user, String snapshotId) {
        if (user == null && snapshotId == null) {
            return transporterActivityEntityService.getAllActivity();
        } else if (user != null && snapshotId == null) {
            return transporterActivityEntityService.getActivityByUser(user);
        } else if (user == null && snapshotId != null) {
            return transporterActivityEntityService.getActivityBySnapshotId(snapshotId);
        } else {
            return transporterActivityEntityService.getActivityByUserAndSnapshotId(user, snapshotId);
        }
    }

}
