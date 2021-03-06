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

package org.jhapy.i18n.repository;

import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@JaversSpringDataAuditable
@Repository
public interface ActionTrlRepository extends JpaRepository<ActionTrl, Long> {

  List<ActionTrl> findByAction(Action action);

  long countByAction(Action action);

  List<ActionTrl> findByIso3Language(String iso3Language);

  @Query("SELECT distinct(iso3Language) from ActionTrl order by iso3Language")
  List<String> getIso3Languages();

  Optional<ActionTrl> getByActionAndIso3Language(Action action, String iso3Language);

  Optional<ActionTrl> getByActionAndIsDefault(Action action, Boolean isDefault);
}
