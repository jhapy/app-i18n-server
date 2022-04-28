package org.jhapy.i18n.repository

import org.jhapy.i18n.domain.ElementLookup
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ElementLookupRepository : JpaRepository<ElementLookup?, UUID?> {
    fun findByElementIdOrName(elementId: UUID?, name: String?): Optional<ElementLookup>
    fun findByName(name: String?): Optional<ElementLookup>
}