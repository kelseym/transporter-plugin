package org.nrg.xnatx.plugins.transporter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Data
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransportActivity implements Serializable {

    private String id;
    private String username;
    private String snapshotId;
    private String sessionId;
    private LocalDateTime timestamp;
    private List<TransportActivityEvent> events;

    public static TransportActivity create(String username, String snapshotId, String sessionId, TransportActivityEvent event) {
        return TransportActivity.builder()
                .username(username)
                .snapshotId(snapshotId)
                .sessionId(sessionId)
                .events(Arrays.asList(event))
                .timestamp(event.getTimestamp())
                .build();
    }

    public static TransportActivity create(TransportActivityMessage activityMessage) {
        return TransportActivity.builder()
                .username(activityMessage.getUsername())
                .snapshotId(activityMessage.getSnapshotId())
                .sessionId(activityMessage.getSessionId())
                .timestamp(activityMessage.getTimestamp())
                .events(Arrays.asList(TransportActivityEvent.create(activityMessage)))
                .build();
    }


    @JsonProperty("snapshot-id-display")
    public String getSnapshotIdsDisplay() {
        if (snapshotId != null && snapshotId.contains("[")) {
            return snapshotId.replace("[", "").replace("]", "");
        }
        return snapshotId;
    }

    @JsonProperty("formatted-timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public String getFormattedTimestamp() {
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timestamp.format(formatter);
        }
        return timestamp != null ? timestamp.toString() : "";
    }

    @JsonProperty("session-id-short")
    public String getSessionIdShort() {
        return sessionId != null ? sessionId.substring(0, Math.min(sessionId.length(), 8)) : "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class TransporterActivityItemCreator implements Serializable {
        private String username;
        private String snapshotId;
        private String sessionId;
        private String event;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class TransportActivityMessage implements Serializable {
        private String username;
        private String snapshotId;
        private String sessionId;
        private LocalDateTime timestamp;
        private String eventMessage;
    }

}
