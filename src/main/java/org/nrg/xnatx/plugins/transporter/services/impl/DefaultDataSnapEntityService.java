package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.transporter.daos.DataSnapEntityDao;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.nrg.xnatx.plugins.transporter.exceptions.UnauthorizedException;
import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.SnapUserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.beans.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DefaultDataSnapEntityService extends AbstractHibernateEntityService<DataSnapEntity, DataSnapEntityDao>  implements DataSnapEntityService {

    private final SnapUserEntityService snapUserEntityService;

    @Autowired
    public DefaultDataSnapEntityService(final SnapUserEntityService snapUserEntityService) {
        this.snapUserEntityService = snapUserEntityService;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public DataSnap createDataSnap(String ownerLogin, DataSnap dataSnap) {
        DataSnapEntity entity = this.create(fromPojo(dataSnap));
        //this.flush();
        entity.setSnapUserEntities(
                Arrays.asList(snapUserEntityService.create(SnapUserEntity.builder()
                        .dataSnapEntity(entity)
                        .login(ownerLogin)
                        .role(SnapUserEntity.Role.OWNER)
                        .build())));
        update(entity);
        return toPojo(entity, true);
    }


    @Override
    public DataSnap addUser(DataSnap dataSnap, String userLogin, SnapUserEntity.Role role) throws NotFoundException {
        snapUserEntityService.create(SnapUserEntity.builder()
                .dataSnapEntity(get(dataSnap.getId()))
                .login(userLogin)
                .role(role)
                .build());
    return toPojo(get(dataSnap.getId()), true);
    }

    @Override
    public void removeUser(DataSnap dataSnap, String userLogin) throws NotFoundException {
        DataSnapEntity entity = get(dataSnap.getId());
        entity.getSnapUserEntities().removeIf(sue -> sue.getLogin().equals(userLogin));
    }

    @Override
    public DataSnap getDataSnap(Long id) throws NotFoundException{

        return toPojo(get(id), true);
    }

    @Override
    public DataSnap getDataSnap(String label) throws NotFoundException {
        return toPojo(getDao().getByLabel(label), true);
    }

    @Override
    public List<DataSnap> getDataSnaps(String userLogin) {
        return toPojo(snapUserEntityService.getDataSnaps(userLogin), false);
    }

    @Override
    public List<DataSnap> getDataSnapsByOwner(String ownerLogin) {
        return toPojo(snapUserEntityService.getDataSnapsByOwner(ownerLogin), false);
    }

    @Override
    public void deleteDataSnap(Long id) throws NotFoundException, UnauthorizedException {
        delete(id);
    }

    @Override
    public void updateDataSnap(DataSnap resolveDataSnap) throws NotFoundException {
        DataSnapEntity entity = get(resolveDataSnap.getId());
        entity.setLabel(resolveDataSnap.getLabel());
        entity.setBuildState(resolveDataSnap.getBuildState());
        entity.setDescription(resolveDataSnap.getDescription());
        entity.setSnap(resolveDataSnap);
        update(entity);
    }

    @Nonnull
    @Transient
    private DataSnapEntity fromPojo(@Nonnull final DataSnap dataSnap) {
        try {
            return DataSnapEntity.builder()
                    .label(dataSnap.getLabel())
                    .description(dataSnap.getDescription())
                    .buildState(dataSnap.getBuildState())
                    .snap(dataSnap)
                    .build();
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
                    DataSnap.builder()
                            .label(entity.getLabel())
                            .id(entity.getId())
                            .buildState(entity.getBuildState())
                            .description(entity.getDescription())
                                    .build();
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
