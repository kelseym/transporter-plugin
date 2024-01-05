package org.nrg.xnatx.plugins.transporter.entities;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Slf4j
@Table(indexes = @Index(name = "session_idx", columnList = "session_id"))
public class TransportActivityEntity extends AbstractHibernateEntity {
    private static final long serialVersionUID = 1L;

    private String username;
    private String sessionId;
    private String snapshotId;
    private LocalDateTime latestEventTimestamp;
    private List<TransportActivityEventEntity> events = new ArrayList<>();

    public TransportActivityEntity() {}

    public TransportActivityEntity(String username, String sessionId, String snapshotId, LocalDateTime latestEventTimestamp) {
        this.username = username;
        this.sessionId = sessionId;
        this.snapshotId = snapshotId;
        this.latestEventTimestamp = latestEventTimestamp;
    }

    @Column
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Column(name = "snapshot_id")
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }

    @Column(name = "session_id", unique = true)
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    @Column(name = "latest_event_timestamp")
    public LocalDateTime getLatestEventTimestamp() { return latestEventTimestamp; }
    public void setLatestEventTimestamp(LocalDateTime latestEventTimestamp) {
        this.latestEventTimestamp = latestEventTimestamp;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transportActivityEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    public List<TransportActivityEventEntity> getEvents() { return events; }
    public void setEvents(List<TransportActivityEventEntity> events) { this.events = events; }

    public void addEvent(TransportActivityEventEntity event) {
        events.add(event);
    }
}
