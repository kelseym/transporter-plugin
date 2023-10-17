package org.nrg.xnatx.plugins.transporter.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.springframework.context.annotation.Lazy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@TypeDefs({@TypeDef(name = "json", typeClass = JsonType.class)})
public class DataSnapEntity extends AbstractHibernateEntity {

    private static final long serialVersionUID = 1L;

    private String owner;
    private String label;
    private String description;
    private DataSnap snap;

    @Column(name = "owner")
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    @Column(name = "label")
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Column(name = "description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "snap")
    public DataSnap getSnap() { return snap; }
    public void setSnap(DataSnap dataSnap) { this.snap = dataSnap; }


}
