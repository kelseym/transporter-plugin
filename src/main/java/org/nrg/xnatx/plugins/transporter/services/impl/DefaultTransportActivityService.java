package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;
import org.nrg.xnatx.plugins.transporter.services.TransportActivityEntityService;
import org.nrg.xnatx.plugins.transporter.services.TransportActivityService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultTransportActivityService implements TransportActivityService {

    // In-memory map of active remote applications
    private Map<String, RemoteAppHeartbeat> remoteApplicationStatusMap = new HashMap<>();

    private final TransportActivityEntityService transportActivityEntityService;

    public DefaultTransportActivityService(TransportActivityEntityService transportActivityEntityService) {
        this.transportActivityEntityService = transportActivityEntityService;
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
    public void updateRemoteApplicationActivity(TransportActivity.TransportActivityMessage activityMessage) {
        transportActivityEntityService.update(activityMessage);
    }

    @Override
    public List<TransportActivity> getRemoteApplicationActivity(String transportSessionId, UserI user, String snapshotId) {
        if (transportSessionId == null && user == null && snapshotId == null) {
            return transportActivityEntityService.getAllActivity();
        } else {
            return transportActivityEntityService.get(transportSessionId, user != null ? user.getLogin() : null, snapshotId);
        }
    }

    @Override
    public void deleteRemoteApplicationActivity(String transportSessionId) {
        transportActivityEntityService.delete(transportSessionId);
    }

}
