package org.jhapy.i18n.repository

import org.jhapy.i18n.domain.MessageLookup
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MessageLookupRepository : JpaRepository<MessageLookup, UUID> {
    fun findByMessageIdOrName(messageID: UUID, name: String): MessageLookup?
    fun findByName(name: String): MessageLookup?
}