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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TypeName("Action")
public class Action extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String name;

  private String category;

  private Boolean translated = Boolean.FALSE;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "parentId")
  @MapKey(name = "iso3Language")
  @ToString.Exclude
  private Map<String, ActionTrl> translations = new HashMap<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    Action action = (Action) o;
    return Objects.equals(getId(), action.getId());
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
