package org.jhapy.i18n.domain

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MessageLookup(
    @Column(columnDefinition = "BINARY(16)")
    @Id val messageId: UUID,
    @Column(unique = true)
    var name: String
) : Serializable