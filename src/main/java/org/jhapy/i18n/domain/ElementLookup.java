package org.jhapy.i18n.domain;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ElementLookup implements Serializable {

  @Id private UUID elementId;

  @Column(unique = true)
  private String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    ElementLookup that = (ElementLookup) o;
    return Objects.equals(elementId, that.elementId);
  }

  @Override
  public int hashCode() {
    return 0;
  }
}