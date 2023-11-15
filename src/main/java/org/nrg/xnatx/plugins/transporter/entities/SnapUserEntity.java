package org.nrg.xnatx.plugins.transporter.entities;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapUserEntity extends AbstractHibernateEntity {

    private static final long serialVersionUID = 1L;

    private Role role;
    private String login;
    private DataSnapEntity dataSnapEntity;

    public enum Role {
        OWNER,
        EDITOR,
        READER
    }


    @Enumerated(EnumType.STRING)
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    @ManyToOne
    public DataSnapEntity getDataSnapEntity() { return dataSnapEntity; }
    public void setDataSnapEntity(DataSnapEntity dataSnapEntity) { this.dataSnapEntity = dataSnapEntity; }
}


