package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.daos.DataSnapEntityDao;
import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.services.DataSnapService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DefaultDataSnapService extends AbstractHibernateEntityService<DataSnapEntity, DataSnapEntityDao>  implements DataSnapService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long addDataSnap(String owner, DataSnap dataSnap) {
        DataSnapEntity entity = create(fromPojo(owner, dataSnap));
        return entity.getId();
    }

    @Override
    public DataSnap getDataSnap(@Nonnull String owner, Long id) throws NotFoundException {
        DataSnapEntity entity = get(id);
        return owner.equals(entity.getOwner()) ? toPojo(entity) : null;
    }

    @Override
    public List<DataSnap> getDataSnaps(String owner) {
        return toPojo(this.getDao().findByOwner(owner));
    }


    @Nonnull
    @Transient
    private DataSnapEntity fromPojo(String owner, @Nonnull final DataSnap dataSnap) {
        try {
            DataSnapEntity entity = new DataSnapEntity();
            entity.setOwner(owner);
            entity.setLabel(dataSnap.getLabel());
            entity.setDescription(dataSnap.getDescription());
            entity.setSnap(dataSnap);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to convert the data snap to an entity", e);
        }
    }

    @Nonnull
    @Transient
    private DataSnap toPojo(@Nonnull final DataSnapEntity entity) {
        try {
            return entity.getSnap().toBuilder().id(entity.getId()).build();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to convert the data snap to an entity", e);
        }
    }

    @Nonnull
    @Transient
    private List<DataSnap> toPojo(@Nonnull final List<DataSnapEntity> entities) {
        return (List<DataSnap>) entities.stream().map(this::toPojo).collect(Collectors.toList());
    }


}
