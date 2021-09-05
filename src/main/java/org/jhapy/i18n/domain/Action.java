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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.TypeName;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "translations")
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TableGenerator(
    name = "ActionKeyGen",
    table = "Sequence",
    pkColumnName = "COLUMN_NAME",
    pkColumnValue = "ACTION_ID",
    valueColumnName = "SEQ_VAL",
    allocationSize = 1)
@TypeName("Action")
public class Action extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String name;

  private String category;

  private Boolean isTranslated = Boolean.FALSE;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "action_trl", joinColumns = @JoinColumn(name = "action_id"))
  @MapKeyColumn(name = "iso3Language")
  private Map<String, ActionTrl> translations = new HashMap<>();
}