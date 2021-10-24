package org.jhapy.i18n.domain;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class FileUpload extends BaseEntity {
  private String filename;

  @Lob private byte[] fileContent;

  private Boolean isValidated = false;

  private Boolean isImported = false;

  private String errorMessage;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    FileUpload that = (FileUpload) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return 0;
  }
}