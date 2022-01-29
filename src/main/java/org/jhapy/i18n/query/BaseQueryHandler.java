package org.jhapy.i18n.query;

import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntityUUIDId;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.i18n.converter.GenericMapper;
import org.jhapy.i18n.domain.BaseEntity;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.utils.RelationalDbSearchCriteria;
import org.jhapy.i18n.utils.RelationalDbSearchOperation;
import org.jhapy.i18n.utils.RelationalDbSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import java.util.List;

public interface BaseQueryHandler<E extends BaseEntity, D extends BaseEntityUUIDId>
    extends HasLogger {
  BaseRepository<E> getRepository();

  EntityManager getEntityManager();

  Class<E> getEntityClass();

  GenericMapper<E, D> getConverter();

  default PageDTO<D> toDtoPage(Page<E> domain, List<D> data) {
    PageDTO<D> result = new PageDTO<>();
    result.setTotalPages(domain.getTotalPages());
    result.setSize(domain.getSize());
    result.setNumber(domain.getNumber());
    result.setNumberOfElements(domain.getNumberOfElements());
    result.setTotalElements(domain.getTotalElements());
    result.setPageable(getConverter().convert(domain.getPageable()));
    result.setContent(data);
    return result;
  }

  default Page<E> findAnyMatching(
      String filter, Boolean showInactive, Pageable pageable, Object... otherCriteria) {
    var loggerString = getLoggerPrefix("findAnyMatching");

    debug(loggerString, "----------------------------------");

    debug(
        loggerString,
        "In, Entity = {0}, Filter = {1}, Show Inactive = {2}, Pageable = {3}",
        getEntityClass().getSimpleName(),
        filter,
        showInactive,
        pageable);

    RelationalDbSpecification<E> criterias = null;
    if (showInactive != null) {
      criterias = new RelationalDbSpecification<>();
      criterias.add(
          new RelationalDbSearchCriteria(
              "isActive", !showInactive, RelationalDbSearchOperation.EQUAL));
    }

    var specifications = buildSearchQuery(filter, otherCriteria);

    Page<E> result;
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
    debug(
        loggerString,
        "Out : Elements = {0} of {1}, Page = {2} of {3}",
        result.getContent().size(),
        result.getTotalElements(),
        result.getNumber(),
        result.getTotalPages());

    return result;
  }

  default long countAnyMatching(String filter, Boolean showInactive, Object... otherCriteria) {
    var loggerString = getLoggerPrefix("countAnyMatching");

    debug(loggerString, "----------------------------------");

    debug(
        loggerString,
        "In, Entity = {0}, Filter = {1}, Show Inactive = {2}",
        getEntityClass().getSimpleName(),
        filter,
        showInactive);

    RelationalDbSpecification<E> criterias = null;

    if (showInactive != null) {
      criterias = new RelationalDbSpecification<>();
      criterias.add(
          new RelationalDbSearchCriteria(
              "isActive", !showInactive, RelationalDbSearchOperation.EQUAL));
    }

    var specifications = buildSearchQuery(filter, otherCriteria);

    long result;
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

    debug(loggerString, "Out = {0} items", result);

    return result;
  }

  Specification<E> buildSearchQuery(String filter, Object... otherCriteria);
}
