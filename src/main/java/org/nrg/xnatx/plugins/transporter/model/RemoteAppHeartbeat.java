package org.nrg.xnatx.plugins.transporter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

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
}

