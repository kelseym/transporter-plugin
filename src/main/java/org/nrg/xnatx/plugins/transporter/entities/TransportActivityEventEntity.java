package org.nrg.xnatx.plugins.transporter.entities;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Slf4j
public class TransportActivityEventEntity extends AbstractHibernateEntity {
    private static final long serialVersionUID = 1L;

    private String message;
    private LocalDateTime eventTimestamp;
    private TransportActivityEntity transportActivityEntity;

    public TransportActivityEventEntity() {}

    public TransportActivityEventEntity(String message, LocalDateTime eventTimestamp, TransportActivityEntity parent) {
        this.message = message;
        this.eventTimestamp = eventTimestamp;
        this.transportActivityEntity = parent;
    }

    @Column
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Column(name = "event_timestamp")
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    @ManyToOne
    public TransportActivityEntity getTransportActivityEntity() { return transportActivityEntity; }
    public void setTransportActivityEntity(TransportActivityEntity transportActivityEntity) {
        this.transportActivityEntity = transportActivityEntity;
    }
}
