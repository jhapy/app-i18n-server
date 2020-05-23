package org.jhapy.i18n.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
@Data
@EqualsAndHashCode(exclude = {"createdBy", "modifiedBy", "created", "modified", "version"})
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

  /**
   * Who create this record (no ID, use username)
   */
  @CreatedBy
  private String createdBy;

  /**
   * When this record has been created
   */
  @CreatedDate
  private Instant created;

  /**
   * How did the last modification of this record (no ID, use username)
   */
  @LastModifiedBy
  private String modifiedBy;

  /**
   * When this record was last updated
   */
  @LastModifiedDate
  private Instant modified;

  /**
   * Version of the record. Used for synchronization and concurrent access.
   */
  @Version
  private Long version;

  /**
   * Indicate if the current record is active (deactivate instead of delete)
   */
  private Boolean isActive = Boolean.TRUE;
}
