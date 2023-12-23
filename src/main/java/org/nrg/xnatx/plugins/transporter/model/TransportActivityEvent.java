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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransportActivityEvent implements Serializable {

    private String message;
    private LocalDateTime timestamp;

    public static TransportActivityEvent create(TransportActivity.TransportActivityMessage message) {
        return TransportActivityEvent.builder()
                .message(message.getEventMessage())
                .timestamp(message.getTimestamp())
                .build();
    }
}
