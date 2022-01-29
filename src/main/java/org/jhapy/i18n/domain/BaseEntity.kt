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
package org.jhapy.i18n.domain

import org.javers.core.metamodel.annotation.DiffIgnore
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.*

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
@MappedSuperclass
@EntityListeners(
    AuditingEntityListener::class
)
abstract class BaseEntity(givenId: UUID? = null) : Serializable {
    @Id
    @Column(name = "id", length = 16, unique = true, nullable = false)
    private var id: UUID = givenId ?: UUID.randomUUID()

    @Transient
    private var persisted: Boolean = givenId != null

    @Column(length = 16)
    var clientId: UUID? = null

    /** Who create this record (no ID, use username)  */
    @DiffIgnore
    @CreatedBy
    var createdBy: String? = null

    /** When this record has been created  */
    @DiffIgnore
    @CreatedDate
    var created: Instant? = null

    /** How did the last modification of this record (no ID, use username)  */
    @DiffIgnore
    @LastModifiedBy
    var modifiedBy: String? = null

    /** When this record was last updated  */
    @DiffIgnore
    @LastModifiedDate
    var modified: Instant? = null

    /** Version of the record. Used for synchronization and concurrent access.  */
    @DiffIgnore
    @Version
    var version: Long? = null

    /** Indicate if the current record is active (deactivate instead of delete)  */
    var isActive = true

    fun getId(): UUID = id

    fun setId(id: UUID) {
        this.id = id
    }

    fun isNew(): Boolean = !persisted

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is BaseEntity -> false
            else -> getId() == other.getId()
        }
    }

    @PostPersist
    @PostLoad
    private fun setPersisted() {
        persisted = true
    }
}