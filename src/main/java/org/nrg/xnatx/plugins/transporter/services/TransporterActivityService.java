package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;

import java.util.List;

public interface TransporterActivityService {

    void updateRemoteApplicationStatus(RemoteAppHeartbeat heartbeat);

    List<RemoteAppHeartbeat> getRemoteApplicationStatus();

    RemoteAppHeartbeat getRemoteApplicationStatus(String remoteAppId);

    void updateRemoteApplicationActivity(TransporterActivityItem activityItem);

    List<TransporterActivityItem> getRemoteApplicationActivity(UserI user, String snapshotId);
}
