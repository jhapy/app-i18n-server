/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.i18n.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
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
public class I18NVersion extends BaseEntity {

  @Column(nullable = false)
  private String tableName;

  @Column(nullable = false)
  private String isoLang;

  private Integer recordVersion;

  private Integer previousRecordVersion;

  private Boolean notificationSent = Boolean.FALSE;
}
