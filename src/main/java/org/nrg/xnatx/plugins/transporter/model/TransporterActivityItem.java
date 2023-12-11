package org.nrg.xnatx.plugins.transporter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransporterActivityItem implements Serializable {

    private String uuid;
    private String username;
    private String snapshotId;
    private String event;
    private LocalDateTime timestamp;
    private RemoteAppHeartbeat remoteAppHeartbeat;

    public static TransporterActivityItem create(String uuid, String username, String snapshotId, String event, RemoteAppHeartbeat remoteAppHeartbeat) {
        return TransporterActivityItem.builder()
                .uuid(uuid)
                .username(username)
                .snapshotId(snapshotId)
                .event(event)
                .remoteAppHeartbeat(remoteAppHeartbeat)
                .build();
    }

    public static TransporterActivityItem create(String username, String uuid, TransporterActivityItemCreator creator) {
        return TransporterActivityItem.builder()
                .uuid(uuid)
                .username(username)
                .snapshotId(creator.getSnapshotId())
                .event(creator.getEvent())
                .timestamp(creator.getTimestamp())
                .remoteAppHeartbeat(creator.getRemoteAppHeartbeat())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class TransporterActivityItemCreator implements Serializable {
        private String username;
        private String snapshotId;
        private String event;
        private LocalDateTime timestamp;
        private RemoteAppHeartbeat remoteAppHeartbeat;
    }

    public String getFormattedTimestamp() {
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timestamp.format(formatter);
        }
        return timestamp != null ? timestamp.toString() : "";
    }
}
