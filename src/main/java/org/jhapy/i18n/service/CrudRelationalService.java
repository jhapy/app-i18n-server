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

import org.jhapy.commons.utils.BeanUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.exception.EntityNotFoundException;
import org.jhapy.i18n.domain.BaseEntity;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.utils.RelationalDbSearchCriteria;
import org.jhapy.i18n.utils.RelationalDbSearchOperation;
import org.jhapy.i18n.utils.RelationalDbSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public interface CrudRelationalService<T extends BaseEntity> extends HasLogger {

  BaseRepository<T> getRepository();

  EntityManager getEntityManager();

  default Class<T> getEntityClass() {
    ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();

    return (Class<T>) superclass.getActualTypeArguments()[0];
  }

  @Transactional
  default T save(T entity) {
    return getRepository().save(entity);
  }

  @Transactional
  default Iterable<T> saveAll(Iterable<T> entity) {
    return getRepository().saveAll(entity);
  }

  @Transactional
  default void delete(T entity) {
    if (entity == null) {
      throw new EntityNotFoundException();
    }
    getRepository().delete(entity);
  }

  @Transactional
  default void delete(UUID id) {
    delete(load(id));
  }

  default long count() {
    return getRepository().count();
  }

  default T load(UUID id) {
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

  default Page<T> findAnyMatching(
      String filter, Boolean showInactive, Pageable pageable, Object... otherCriteria) {
    var loggerString = getLoggerPrefix("findAnyMatching");

    logger().debug(loggerString + "----------------------------------");

    debug(
        loggerString,
        "In, Entity = {0}, Filter = {1}, Show Inactive = {2}, Pageable = {3}",
        getEntityClass().getSimpleName(),
        filter,
        showInactive,
        pageable);

    RelationalDbSpecification<T> criterias = null;
    if (showInactive != null) {
      if (criterias == null) criterias = new RelationalDbSpecification<T>();
      criterias.add(
          new RelationalDbSearchCriteria(
              "isActive", !showInactive, RelationalDbSearchOperation.EQUAL));
    }

    var specifications = buildSearchQuery(filter, otherCriteria);

    Page<T> result;
    if (specifications != null) {
      if (criterias != null) {
        specifications.and(criterias);
      }
      result =
          getRepository().findAll(specifications, pageable == null ? Pageable.unpaged() : pageable);
    } else {

      if (criterias != null) {
        result =
            getRepository().findAll(criterias, pageable == null ? Pageable.unpaged() : pageable);
      } else result = getRepository().findAll(pageable == null ? Pageable.unpaged() : pageable);
    }
    logger()
        .debug(
            loggerString
                + "Out : Elements = "
                + result.getContent().size()
                + " of "
                + result.getTotalElements()
                + ", Page = "
                + result.getNumber()
                + " of "
                + result.getTotalPages());

    return result;
  }

  default List<T> findAnyMatchingNoPaging(
      String filter, Boolean showInactive, Object... otherCriteria) {
    var loggerString = getLoggerPrefix("findAnyMatchingNoPaging");

    logger().debug(loggerString + "----------------------------------");

    debug(
        loggerString,
        "In, Entity = {0}, Filter = {1}, Show Inactive = {2}",
        getEntityClass().getSimpleName(),
        filter,
        showInactive);

    RelationalDbSpecification<T> criterias = null;

    if (showInactive != null) {
      if (criterias == null) criterias = new RelationalDbSpecification<T>();
      criterias.add(
          new RelationalDbSearchCriteria(
              "isActive", !showInactive, RelationalDbSearchOperation.EQUAL));
    }

    var specifications = buildSearchQuery(filter, otherCriteria);

    List<T> result;
    if (specifications != null) {
      if (criterias != null) {
        specifications.and(criterias);
      }
      result = getRepository().findAll(specifications);
    } else {
      if (criterias != null) {
        result = getRepository().findAll(criterias);
      } else result = getRepository().findAll();
    }
    logger().debug(loggerString + "Out : Elements = " + result.size());

    return result;
  }

  default long countAnyMatching(String filter, Boolean showInactive, Object... otherCriteria) {
    var loggerString = getLoggerPrefix("countAnyMatching");

    logger().debug(loggerString + "----------------------------------");

    debug(
        loggerString,
        "In, Entity = {0}, Filter = {1}, Show Inactive = {2}",
        getEntityClass().getSimpleName(),
        filter,
        showInactive);

    RelationalDbSpecification<T> criterias = null;

    if (showInactive != null) {
      if (criterias == null) criterias = new RelationalDbSpecification<T>();
      criterias.add(
          new RelationalDbSearchCriteria(
              "isActive", !showInactive, RelationalDbSearchOperation.EQUAL));
    }

    var specifications = buildSearchQuery(filter, otherCriteria);

    Long result;
    if (specifications != null) {
      if (criterias != null) {
        specifications.and(criterias);
      }
      result = getRepository().count(specifications);
    } else {
      if (criterias != null) {
        result = getRepository().count(criterias);
      } else result = getRepository().count();
    }

    logger().debug(loggerString + "Out = " + result + " items");

    return result;
  }

  default Specification<T> buildSearchQuery(String filter, Object... otherCriteria) {
    return null;
  }

  default void convert(Map<String, Object> entity, T existingRecord) {
    BeanUtils.copyNonNullProperties(existingRecord, entity);
  }
}
