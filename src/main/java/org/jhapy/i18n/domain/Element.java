package org.jhapy.i18n.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TableGenerator(name = "ElementKeyGen", table = "Sequence", pkColumnName = "COLUMN_NAME", pkColumnValue = "ELEMENT_ID", valueColumnName = "SEQ_VAL", initialValue = 0, allocationSize = 1)
public class Element extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ElementKeyGen")
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  private String category;

  private Boolean isTranslated = Boolean.FALSE;

  @Transient
  private List<ElementTrl> translations = new ArrayList<>();
}
