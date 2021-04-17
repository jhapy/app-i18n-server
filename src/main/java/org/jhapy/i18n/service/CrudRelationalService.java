/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.i18n.service;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.BeanUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.domain.exception.EntityNotFoundException;
import org.jhapy.i18n.domain.BaseEntity;
import org.jhapy.i18n.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public interface CrudRelationalService<T extends BaseEntity> extends HasLogger {
  JpaRepository<T, Long> getRepository();

  EntityManager getEntityManager();

  Class<T> getEntityClass();

  @Transactional
  default T save(T entity) {
    return getRepository().save(entity);
  }

  @Transactional
  default void delete(T entity) {
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    getRepository().delete(entity);
  }

  @Transactional
  default void delete(long id) {
    delete(load(id));
  }

  default long count() {
    return getRepository().count();
  }

  default T load(long id) {
    T entity = getRepository().findById(id).orElse(null);
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    return entity;
  }

  default List<T> getAll() {
    return getRepository().findAll();
  }

  default boolean hasChanged(Object previousValue, Object currentValue) {
    if (previousValue == null && currentValue == null) {
      return false;
    }
    if (previousValue == null) {
      return true;
    }
    if (currentValue == null) {
      return true;
    }
    if (previousValue instanceof Number) {
      return ((Number) previousValue).longValue() != ((Number) currentValue).longValue();
    }
    if (previousValue instanceof LocalDate) {
      return ((LocalDate) previousValue).compareTo(((LocalDate) currentValue)) != 0;
    }
    if (previousValue instanceof String) {
      return !((String) previousValue).equalsIgnoreCase((String) currentValue);
    }

    return !previousValue.equals(currentValue);
  }

  default Page<T> findAnyMatching(String currentUserId, String filter, Boolean showInactive,
      Pageable pageable, Object... otherCriteria) {
    String loggerString = getLoggerPrefix("findAnyMatching");

    logger().debug(
        loggerString + "----------------------------------");

    String currentUser = SecurityUtils.getCurrentUserLogin().get();

    logger().debug(
        loggerString + "In, Filter = " + filter + ", Show Inactive = "
            + showInactive + ", Pageable = " + pageable);

    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery(getEntityClass());
    Root<T> entity = query.from(getEntityClass());

    CriteriaQuery criteriaQuery = buildSearchQuery(query, entity, cb, currentUser, filter,
        showInactive, otherCriteria);
    criteriaQuery.select(cb.count(entity));
    TypedQuery<Long> countTypedQuery = getEntityManager().createQuery(criteriaQuery);
    Long nbRecords = countTypedQuery.getSingleResult();

    query.orderBy(QueryUtils.toOrders(pageable.getSort(), entity, cb));
    query.distinct(true);
    criteriaQuery.select(entity);

    TypedQuery<T> typedQuery = getEntityManager().createQuery(criteriaQuery);
    if (pageable.isPaged()) {
      typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
      typedQuery.setMaxResults(pageable.getPageSize());
    } else {
    }

    Page<T> result = new PageImpl<>(typedQuery.getResultList(), pageable, nbRecords);

    logger()
        .debug(loggerString + "Out : Elements = " + result.getContent().size() + " of " + result
            .getTotalElements() + ", Page = " + result.getNumber() + " of " + result
            .getTotalPages());

    return result;
  }

  default List<T> findAnyMatchingNoPaging(String currentUserId, String filter, Boolean showInactive,
      Object... otherCriteria) {
    String loggerString = getLoggerPrefix("findAnyMatchingNoPaging");

    logger().debug(
        loggerString + "----------------------------------");

    String currentUser = SecurityUtils.getCurrentUserLogin().get();

    logger().debug(
        loggerString + "In, Filter = " + filter + ", Show Inactive = "
            + showInactive);

    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery(getEntityClass());
    Root<T> entity = query.from(getEntityClass());

    CriteriaQuery criteriaQuery = buildSearchQuery(query, entity, cb, currentUser, filter,
        showInactive, otherCriteria);
    criteriaQuery.select(entity);

    TypedQuery<T> typedQuery = getEntityManager().createQuery(criteriaQuery);

    List<T> result = typedQuery.getResultList();

    logger()
        .debug(loggerString + "Out : Elements = " + result.size());

    return result;
  }

  default long countAnyMatching(String currentUserId, String filter, Boolean showInactive,
      Object... otherCriteria) {
    String loggerString = getLoggerPrefix("countAnyMatching");

    logger().debug(
        loggerString + "----------------------------------");

    String currentUser = SecurityUtils.getCurrentUserLogin().get();

    logger().debug(
        loggerString + "In, Filter = " + filter + ", Show Inactive = "
            + showInactive);

    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T> query = cb.createQuery(getEntityClass());
    Root<T> entity = query.from(getEntityClass());

    CriteriaQuery criteriaQuery = buildSearchQuery(query, entity, cb, currentUser, filter,
        showInactive, otherCriteria);
    criteriaQuery.select(cb.countDistinct(entity));
    TypedQuery<Long> q = getEntityManager().createQuery(criteriaQuery);

    Long result = q.getSingleResult();

    logger().debug(loggerString + "Out = " + result + " items");

    return result;
  }

  CriteriaQuery buildSearchQuery(CriteriaQuery query, Root<T> entity, CriteriaBuilder cb,
      String currentUserId, String filter, Boolean showInactive, Object... otherCriteria);

  default T update(OrikaBeanMapper mapperFacade, Map<String, Object> entity)
      throws ServiceException {
    // Not working yet
    if (!entity.containsKey("id") || entity.get("id") == null) {
      throw new ServiceException("At least an ID attribute is required with a value");
    }

    long id;
    try {
      id = Long.parseLong(entity.get("id").toString());
    } catch (NumberFormatException nfe) {
      throw new ServiceException("ID attribute value is invalid");
    }
    T existingRecord = getRepository().findById(id).get();
    convert(entity, existingRecord);

    return save(existingRecord);
  }

  default void convert(Map<String, Object> entity, T existingRecord) {
    BeanUtils.copyNonNullProperties(existingRecord, entity);
  }
}
