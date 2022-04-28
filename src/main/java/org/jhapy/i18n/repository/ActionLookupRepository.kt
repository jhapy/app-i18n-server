package org.jhapy.i18n.repository

import org.jhapy.i18n.domain.ActionLookup
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ActionLookupRepository : JpaRepository<ActionLookup, UUID> {
    fun findByActionIdOrName(actionId: UUID, name: String): Optional<ActionLookup>
    fun findByName(name: String): Optional<ActionLookup>
}