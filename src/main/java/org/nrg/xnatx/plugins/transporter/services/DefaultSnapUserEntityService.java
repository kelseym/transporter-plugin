package org.nrg.xnatx.plugins.transporter.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.transporter.daos.SnapUserEntityDao;
import org.nrg.xnatx.plugins.transporter.entities.DataSnapEntity;
import org.nrg.xnatx.plugins.transporter.entities.SnapUserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;


@Slf4j
@Service
@Transactional
public class DefaultSnapUserEntityService extends AbstractHibernateEntityService<SnapUserEntity, SnapUserEntityDao> implements SnapUserEntityService {

    @Override
    public SnapUserEntity create(SnapUserEntity entity) {
        getDao().create(entity);
        return entity;
    }

    @Override
    public List<DataSnapEntity> getDataSnaps(String login) {
        List<SnapUserEntity> snapUserEntities = getDao().findByLogin(login);
        return snapUserEntities == null ? Collections.emptyList() :
                snapUserEntities.stream()
                        .map(SnapUserEntity::getDataSnapEntity)
                        .filter(distinctByKey(DataSnapEntity::getId))
                        .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<DataSnapEntity> getDataSnapsByOwner(String login) {
        List<SnapUserEntity> snapOwnerEntities = getDao().findByLogin(login).stream()
                .filter(snapUserEntity -> snapUserEntity.getRole().equals(SnapUserEntity.Role.OWNER))
                .collect(java.util.stream.Collectors.toList());
        return snapOwnerEntities == null ? Collections.emptyList() :
                snapOwnerEntities.stream()
                        .map(SnapUserEntity::getDataSnapEntity)
                        .filter(distinctByKey(DataSnapEntity::getId))
                        .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SnapUserEntity> findByDataSnapId(Long id) {
        return getDao().findByDataSnapId(id);
    }

    @Override
    public List<SnapUserEntity> findByDataSnapLabel(String label) {
        return getDao().findByDataSnapLabel(label);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

}
