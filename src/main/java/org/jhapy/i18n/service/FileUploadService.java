package org.jhapy.i18n.service;

import org.jhapy.dto.serviceResponse.FileUploadStatusResponse;
import org.jhapy.i18n.domain.FileUpload;

import java.io.IOException;
import java.util.UUID;

public interface FileUploadService extends CrudRelationalService<FileUpload> {
  UUID uploadFile(String filename, Byte[] fileContent);

  void validate(UUID id);

  void importFile(UUID id) throws IOException;

  FileUploadStatusResponse getFileUploadStatus(UUID id);
}