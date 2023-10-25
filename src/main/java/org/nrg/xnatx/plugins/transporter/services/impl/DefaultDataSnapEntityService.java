package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.transporter.daos.DataSnapEntityDao;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DefaultDataSnapEntityService extends AbstractHibernateEntityService<DataSnapEntity, DataSnapEntityDao>  implements DataSnapEntityService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DataSnap addDataSnap(String owner, DataSnap dataSnap) {
        DataSnapEntity entity = create(fromPojo(owner, dataSnap));
        return toPojo(entity, true);
    }

    @Override
    public DataSnap getDataSnap(@Nonnull String owner, Long id) throws NotFoundException{
        DataSnapEntity entity = get(id);
        return owner.equals(entity.getOwner()) ? toPojo(entity, true) : null;
    }

    @Override
    public DataSnap getDataSnap(String owner, String label) throws NotFoundException {
        DataSnapEntity entity = getDao().getByLabel(label);
        return owner.equals(entity.getOwner()) ? toPojo(entity, true) : null;
    }

    @Override
    public List<DataSnap> getDataSnaps(String owner) {
        List<DataSnapEntity> snaps = this.getDao().findByOwner(owner);
        return snaps != null ?
                toPojo(snaps, false) :
                Lists.newArrayList();
    }

    @Override
    public void deleteDataSnap(@Nonnull String owner, Long id) throws NotFoundException, UnauthorizedException {
        DataSnapEntity entity = get(id);
        if (owner.equals(entity.getOwner())) {
            delete(entity);
        } else {
            throw new UnauthorizedException("User " + owner + " is not authorized to delete data snap " + id);
        }
    }

    @Override
    public void deleteDataSnaps(@Nonnull String owner) throws UnauthorizedException {
        List<DataSnapEntity> entities = getDao().findByOwner(owner);
        for (DataSnapEntity entity : entities) {
            log.debug("Deleting data snap {}", entity.getLabel());
            delete(entity);
        }
    }

    @Override
    public void updateDataSnap(String login, DataSnap resolveDataSnap) throws NotFoundException {
        DataSnapEntity entity = get(resolveDataSnap.getId());
        if (login.equals(entity.getOwner())) {
            entity.setLabel(resolveDataSnap.getLabel());
            entity.setDescription(resolveDataSnap.getDescription());
            entity.setSnap(resolveDataSnap);
            update(entity);
        }
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
    private DataSnap toPojo(@Nonnull final DataSnapEntity entity, Boolean includeFullSnap) {
        try {
            return includeFullSnap ?
                    entity.getSnap().toBuilder().id(entity.getId()).build() :
                    DataSnap.builder().label(entity.getLabel())
                            .description(entity.getDescription()).id(entity.getId()).build();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to convert the data snap to an entity", e);
        }
    }

    @Nonnull
    @Transient
    private List<DataSnap> toPojo(@Nonnull final List<DataSnapEntity> entities, Boolean includeFullSnap) {
        return (List<DataSnap>) entities.stream().map(snapEntity -> toPojo(snapEntity, includeFullSnap))
                .collect(Collectors.toList());
    }


}
