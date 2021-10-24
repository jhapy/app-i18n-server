package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.cqrs.query.i18n.*;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.converter.GenericMapper;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.repository.ActionRepository;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.utils.RelationalDbSearchCriteria;
import org.jhapy.i18n.utils.RelationalDbSearchOperation;
import org.jhapy.i18n.utils.RelationalDbSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ActionQueryHandler implements BaseQueryHandler<Action, ActionDTO> {
  private final ActionRepository repository;
  private final ActionConverter converter;
  private final EntityManager entityManager;

  @QueryHandler
  public ActionDTO getById(GetActionByIdQuery query) {
    return converter.asDTO(repository.getById(query.getId()), null);
  }

  @QueryHandler
  public ActionDTO getByName(GetActionByNameQuery query) {
    return converter.asDTO(repository.getByName(query.getName()).orElseThrow(), null);
  }

  @QueryHandler
  public List getAll(GetAllActionsQuery query) {
    return converter.asDTOList(repository.findAll(), null);
  }

  @QueryHandler
  public PageDTO findAnyMatchingAction(FindAnyMatchingActionQuery query) {
    Page<Action> result =
        BaseQueryHandler.super.findAnyMatching(
            query.getFilter(), query.getShowInactive(), converter.convert(query.getPageable()));
    return toDtoPage(result, converter.asDTOList(result.getContent(), null));
  }

  @QueryHandler
  public long countAnyMatchingAction(CountAnyMatchingActionQuery query) {
    return BaseQueryHandler.super.countAnyMatching(query.getFilter(), query.getShowInactive());
  }

  @Override
  public Specification<Action> buildSearchQuery(String filter, Object... otherCriteria) {
    if (StringUtils.isNotBlank(filter)) {
      var criterias = new ArrayList<>();
      criterias.add(
          new RelationalDbSearchCriteria("name", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria("category", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria(
              "translations.value", filter, RelationalDbSearchOperation.MATCH));

      var result = new RelationalDbSpecification<Action>();
      result.or(criterias.toArray(new RelationalDbSearchCriteria[0]));
      return result;
    } else {
      return null;
    }
  }

  @Override
  public BaseRepository<Action> getRepository() {
    return repository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Class<Action> getEntityClass() {
    return Action.class;
  }

  @Override
  public GenericMapper<Action, ActionDTO> getConverter() {
    return converter;
  }
}