package org.nrg.xnatx.plugins.transporter.services.impl;


import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.transporter.daos.TransportActivityEntityDao;
import org.nrg.xnatx.plugins.transporter.entities.TransportActivityEntity;
import org.nrg.xnatx.plugins.transporter.entities.TransportActivityEventEntity;
import org.nrg.xnatx.plugins.transporter.model.TransportActivity;
import org.nrg.xnatx.plugins.transporter.model.TransportActivityEvent;
import org.nrg.xnatx.plugins.transporter.services.TransportActivityEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
public class DefaultTransportActivityEntityService
        extends AbstractHibernateEntityService<TransportActivityEntity, TransportActivityEntityDao>
        implements TransportActivityEntityService {

    @Override
    public TransportActivity create(TransportActivity activity) {
        return toPojo(super.create(fromPojo(activity)));
    }

    @Override
    public void update(@Nonnull TransportActivity.TransportActivityMessage activityMessage) {
        TransportActivityEntity activityEntity = getDao().getBySessionId(activityMessage.getSessionId());
        if (activityEntity == null) {
            create(TransportActivity.create(activityMessage));
        } else {
            if (Strings.isNullOrEmpty(activityEntity.getSnapshotId()) && !Strings.isNullOrEmpty(activityMessage.getSnapshotId())) {
                activityEntity.setSnapshotId(activityMessage.getSnapshotId());
            }
            activityEntity.addEvent(fromPojo(TransportActivityEvent.create(activityMessage), activityEntity));
            activityEntity.setLatestEventTimestamp(activityMessage.getTimestamp());
            update(activityEntity);
        }
    }

    @Override
    public List<TransportActivity> getAllActivity() {
        return toPojo(getAll());
    }

    @Override
    public List<TransportActivity> get(String sessionId, String user, String snapshotId) {
        return toPojo(getDao().get(sessionId, user, snapshotId));
    }


    @Override
    public TransportActivityEntity fromPojo(TransportActivity activity) {
        TransportActivityEntity entity = new TransportActivityEntity();
        entity.setUsername(activity.getUsername());
        entity.setSessionId(activity.getSessionId());
        entity.setSnapshotId(activity.getSnapshotId());
        entity.setLatestEventTimestamp(activity.getTimestamp());
        entity.setEvents(fromPojo(activity.getEvents(), entity));
        return entity;
    }

    @Override
    public void delete(String transportSessionId) {
        TransportActivityEntity activityEntity = getDao().getBySessionId(transportSessionId);
        if (activityEntity != null) {
            delete(activityEntity);
        }
    }

    private List<TransportActivityEventEntity> fromPojo(List<TransportActivityEvent> events, TransportActivityEntity parentEvent) {
        return events.stream().map(ev -> fromPojo(ev, parentEvent)).collect(java.util.stream.Collectors.toList());
    }

    private TransportActivityEventEntity fromPojo(TransportActivityEvent event, TransportActivityEntity parentEvent) {
        return new TransportActivityEventEntity(event.getMessage(), event.getTimestamp(), parentEvent);
    }
    private TransportActivity toPojo(TransportActivityEntity entity) {
        return entity == null ? null :
                TransportActivity.builder()
                .id(Long.toString(entity.getId()))
                .username(entity.getUsername())
                .sessionId(entity.getSessionId())
                .snapshotId(entity.getSnapshotId())
                .timestamp(entity.getLatestEventTimestamp())
                .events(toEventPojo(entity.getEvents()))
                .build();
    }

    private List<TransportActivity> toPojo(List<TransportActivityEntity> entities) {
        return entities == null ? Collections.emptyList() :
                entities.stream().map(this::toPojo).collect(java.util.stream.Collectors.toList());
    }

    private List<TransportActivityEvent> toEventPojo(List<TransportActivityEventEntity> entities) {
        return entities == null ? Collections.emptyList() :
                entities.stream().map(this::toPojo).collect(java.util.stream.Collectors.toList());
    }

    private TransportActivityEvent toPojo(TransportActivityEventEntity entity) {
        return TransportActivityEvent.builder()
                .message(entity.getMessage())
                .timestamp(entity.getEventTimestamp())
                .build();
    }

}
