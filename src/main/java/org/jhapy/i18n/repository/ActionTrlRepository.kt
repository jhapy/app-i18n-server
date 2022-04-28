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
package org.jhapy.i18n.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.jhapy.i18n.domain.ActionTrl
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@JaversSpringDataAuditable
@Repository
interface ActionTrlRepository : BaseRepository<ActionTrl> {
    fun getByParentIdAndIso3Language(parentId: UUID, iso3Language: String): Optional<ActionTrl>
    fun getByParentIdAndIsDefaultIsTrue(parentId: UUID): Optional<ActionTrl>
    fun findByIso3Language(iso3Language: String): Iterable<ActionTrl>
    fun findByParentId(parentId: UUID): Collection<ActionTrl>

    @get:Query("SELECT DISTINCT iso3Language FROM ActionTrl")
    val distinctIso3Language: List<String>
}