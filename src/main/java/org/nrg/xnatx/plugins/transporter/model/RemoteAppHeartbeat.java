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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteAppHeartbeat implements Serializable {
    private String status;
    private String remoteAppId;
    private String remoteHost;
    private String xnatHost;
    private String xnatConnectionStatus;
    private String message;
    private LocalDateTime timestamp;
    private long uptime;

    public String getFormattedTimestamp() {
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timestamp.format(formatter);
        }
        return timestamp != null ? timestamp.toString() : "";
    }

    public String getFormattedUptime() {
        long days = uptime / (24 * 3600);
        long hours = (uptime % (24 * 3600)) / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;

        return String.format("%d days, %02d h, %02d m, %02d s", days, hours, minutes, seconds);
    }
}

