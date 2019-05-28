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

import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 *
 * A state must implement [Policy] or one of its descendants.
 *
 * @Param alice Data Owner
 * @param enrico Data Produced
 * @param bob Data Recipient
 * @param policyName The Policy Name
 * @param policyExpirationDate The Expiration Date of the Policy
 * @param policyPassword The password of the Policy
 * @param policyId The Policy Id
 *
 *
 */


@CordaSerializable
data class Policy(val alice: Party,
                  val enrico: Party,
                  val bob: Party,
                  val policyName: String,
                  val policyExpirationDate: String,
                  val policyPassword: String,
                  val policyId: String) : QueryableState {


    override val participants: List<AbstractParty> get() = listOf(alice, enrico, bob)

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PolicySchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PolicySchemaV1 -> PolicySchemaV1.PersistentPolicy(
                    alice = this.alice .toString(),
                    enrico = this.enrico.toString(),
                    bob = this.policyName,
                    policyName = this.policyName,
                    policyExpirationDate = this.policyExpirationDate,
                    policyPassword = this.policyPassword,
                    policyId = this.policyId
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

}
