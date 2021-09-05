package org.jhapy.i18n.converter;

import org.jhapy.commons.utils.HasLoggerStatic;
import org.jhapy.dto.domain.BaseEntityLongId;
import org.jhapy.i18n.domain.BaseEntity;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;

@Component
@ConditionalOnProperty("spring.datasource.url")
public class RelationalDbReferenceMapper {

  private final EntityManager em;

  public RelationalDbReferenceMapper(EntityManager em) {
    this.em = em;
  }

  @ObjectFactory
  public <T extends BaseEntity> T resolve(BaseEntityLongId sourceDTO, @TargetType Class<T> type) {
    String loggerPrefix = HasLoggerStatic.getLoggerPrefix("resolve");
    T entity = null;
    if (sourceDTO.getId() != null) {
      entity = em.find(type, sourceDTO.getId());
    }
    try {
      if (entity == null) {
        entity = type.getDeclaredConstructor().newInstance();
      }
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      HasLoggerStatic.error(
          RelationalDbReferenceMapper.class,
          loggerPrefix,
          "Unexpected error : " + e.getMessage(),
          e);
    }
    return entity;
  }
}