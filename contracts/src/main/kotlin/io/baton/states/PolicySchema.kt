/**
 *   Copyright 2019, Dapps Incorporated.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
 * The family of schemas for [PolicySchema].
 */

object PolicySchema

/**
 * First version of an [PolicySchema] schema.
 */


object PolicySchemaV1 : MappedSchema(PolicySchema.javaClass, 1, listOf(PersistentPolicy::class.java)) {
    @Entity
    @Table(name = "policys", indexes = arrayOf(Index(name = "idx_policy_alice", columnList = "alice"),
            Index(name = "idx_policy_bob", columnList = "bob")))
    class PersistentPolicy(
            @Column(name = "alice")
            var alice: String,

            @Column(name = "enrico")
            var enrico: String,

            @Column(name = "bob")
            var bob: String,

            @Column(name = "policyName")
            var policyName: String,

            @Column(name = "policyExpirationDate")
            var policyExpirationDate: String,

            @Column(name = "policyPassword")
            var policyPassword: String,

            @Column(name = "policyId")
            var policyId: String

    ) : PersistentState() {
        constructor() : this("default-constructor-required-for-hibernate", "", "", "", "", "", "")
    }

}