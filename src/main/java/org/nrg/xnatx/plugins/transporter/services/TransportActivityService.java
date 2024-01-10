package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityPaginatedRequest;

import java.util.List;

public interface TransportActivityService {

    void updateRemoteApplicationStatus(RemoteAppHeartbeat heartbeat);

    List<RemoteAppHeartbeat> getRemoteApplicationStatus();

    RemoteAppHeartbeat getRemoteApplicationStatus(String remoteAppId);

    void updateRemoteApplicationActivity(TransportActivity.TransportActivityMessage activityMessage);

    List<TransportActivity> getRemoteApplicationActivity(String transportSessionId, UserI user, String snapshotId);

    void deleteRemoteApplicationActivity(String transportSessionId);

    List<TransportActivity> getPaginated(TransporterActivityPaginatedRequest transporterActivityPaginatedRequest);
}
