package org.nrg.xnatx.plugins.transporter.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.transporter.model.TransporterActivityItem;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class TransporterActivityEntity extends AbstractHibernateEntity {
    private static final long serialVersionUID = 1L;

    private String uuid;
    private String username;
    private String snapshotId;
    private String event;
    private LocalDateTime eventTimestamp;

    @Column(unique = true)
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    @Column
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Column(name = "snapshot_id")
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }

    @Column
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event;}

    @Column(name = "event_timestamp")
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime timestamp) { this.eventTimestamp = timestamp; }

    public static TransporterActivityEntity fromPojo(TransporterActivityItem activityItem) {
        return TransporterActivityEntity.builder()
                .uuid(activityItem.getUuid())
                .username(activityItem.getUsername())
                .snapshotId(activityItem.getSnapshotId())
                .event(activityItem.getEvent())
                .eventTimestamp(activityItem.getTimestamp())
                .build();
    }

    public static List<TransporterActivityItem> toPojo(@Nonnull List<TransporterActivityEntity> entities) {
        return entities.stream().map(entity -> TransporterActivityEntity.toPojo(entity)).collect(Collectors.toList());
    }

    public static TransporterActivityItem toPojo(TransporterActivityEntity entity) {
        return TransporterActivityItem.builder()
                .uuid(entity.getUuid())
                .username(entity.getUsername())
                .snapshotId(entity.getSnapshotId())
                .event(entity.getEvent())
                .timestamp(entity.getEventTimestamp())
                .build();
    }

    public TransporterActivityItem toPojo() {
        return TransporterActivityEntity.toPojo(this);
    }
}
