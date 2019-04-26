package io.baton.states

import net.corda.core.crypto.NullKeys
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

/**
 * The family of schemas for [UPISchema].
 */

object UPISchema

/**
 * First version of an [UPISchema] schema.
 */


object UPISchemaV1 : MappedSchema(UPISchema.javaClass, 1, listOf(PersistentUPI::class.java)) {
    @Entity
    @Table(name = "upis", indexes = arrayOf(Index(name = "idx_upi_pa", columnList = "pa"),
            Index(name = "idx_upi_pn", columnList = "pn")))
    class PersistentUPI(
            @Column(name = "pa")
            var pa: String,

            @Column(name = "pn")
            var pn: String,

            @Column(name = "mc")
            var mc: String,

            @Column(name = "tr")
            var tr: String,

            @Column(name = "tid")
            var tid: String,

            @Column(name = "tn")
            var tn: String,

            @Column(name = "am")
            var am: String,

            @Column(name = "mam")
            var mam: String,

            @Column(name = "cu")
            var cu: String,

            @Column(name = "url")
            var url: String

    ) : PersistentState() {
        constructor() : this("default-constructor-required-for-hibernate", "", "", "", "", "", "", "", "", "")
    }

}