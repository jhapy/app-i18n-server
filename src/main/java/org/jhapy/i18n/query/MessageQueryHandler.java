package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.cqrs.query.i18n.*;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.i18n.converter.GenericMapper;
import org.jhapy.i18n.converter.MessageConverter;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.utils.RelationalDbSearchCriteria;
import org.jhapy.i18n.utils.RelationalDbSearchOperation;
import org.jhapy.i18n.utils.RelationalDbSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class MessageQueryHandler implements BaseQueryHandler<Message, MessageDTO> {
  private final MessageRepository repository;
  private final MessageConverter converter;
  private final EntityManager entityManager;

  @QueryHandler
  public GetMessageByIdResponse getById(GetMessageByIdQuery query) {
    return new GetMessageByIdResponse(converter.asDTO(repository.getById(query.getId()), null));
  }

  @QueryHandler
  public GetMessageByNameResponse getByName(GetMessageByNameQuery query) {
    Message message = repository.getByName(query.getName());
    return new GetMessageByNameResponse(message == null ? null : converter.asDTO(message, null));
  }

  @QueryHandler
  public GetAllMessagesResponse getAll(GetAllMessagesQuery query) {
    return new GetAllMessagesResponse(converter.asDTOList(repository.findAll(), null));
  }

  @QueryHandler
  public FindAnyMatchingMessageResponse findAnyMatchingMessage(FindAnyMatchingMessageQuery query) {
    Page<Message> result =
        BaseQueryHandler.super.findAnyMatching(
            query.getFilter(), query.getShowInactive(), converter.convert(query.getPageable()));
    return new FindAnyMatchingMessageResponse(converter.asDTOList(result.getContent(), null));
  }

  @QueryHandler
  public CountAnyMatchingMessageResponse countAnyMatchingMessage(
      CountAnyMatchingMessageQuery query) {
    return new CountAnyMatchingMessageResponse(
        BaseQueryHandler.super.countAnyMatching(query.getFilter(), query.getShowInactive()));
  }

  @Override
  public Specification<Message> buildSearchQuery(String filter, Object... otherCriteria) {
    if (StringUtils.isNotBlank(filter)) {
      var criterias = new ArrayList<>();
      criterias.add(
          new RelationalDbSearchCriteria("name", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria("category", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria(
              "translations.value", filter, RelationalDbSearchOperation.MATCH));

      var result = new RelationalDbSpecification<Message>();
      result.or(criterias.toArray(new RelationalDbSearchCriteria[0]));
      return result;
    } else {
      return null;
    }
  }

  @Override
  public BaseRepository<Message> getRepository() {
    return repository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Class<Message> getEntityClass() {
    return Message.class;
  }

  @Override
  public GenericMapper<Message, MessageDTO> getConverter() {
    return converter;
  }
}
