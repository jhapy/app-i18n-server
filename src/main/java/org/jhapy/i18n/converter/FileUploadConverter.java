package org.jhapy.i18n.converter;

import org.jhapy.cqrs.command.SubmitUploadCommand;
import org.jhapy.cqrs.event.i18n.UploadSubmittedEvent;
import org.jhapy.i18n.command.FileUploadAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FileUploadConverter {
  FileUploadConverter INSTANCE = Mappers.getMapper(FileUploadConverter.class);

  @Mapping(target = "validated", ignore = true)
  @Mapping(target = "imported", ignore = true)
  @Mapping(target = "errorMessage", ignore = true)
  UploadSubmittedEvent toUploadSubmittedEvent(SubmitUploadCommand command);

  @Mapping(target = "version", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "fileUploadService", ignore = true)
  void updateAggregateFromUploadSubmittedEvent(
      UploadSubmittedEvent event, @MappingTarget FileUploadAggregate aggregate);
}
