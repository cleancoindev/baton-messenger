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

/**
 *
 * A state must implement [UPI] or one of its descendants.
 *
 * @Param pa Payee VPA (Virtual Payment Address)
 * @param pn Payee Name
 * @param mc Payee Merchant Code
 * @param tid PSP Generated Id
 * @param tr Transaction Id Reference
 * @param tn Transaction Note / Description
 * @param am Transaction Amount
 * @param mam Minimum Amount to Be Paid
 * @param cu Currency Code Currently Only "INR"
 * @param url This should be a URL for Transaction Details
 *
 *
 */


@CordaSerializable
data class UPI(val pa: Party,
               val pn: Party,
               val mc: String,
               val tid: String,
               val tr: String,
               val tn: String,
               val am: String,
               val mam: String,
               val cu: String,
               val url: String) : QueryableState {


    override val participants: List<AbstractParty> get() = listOf(pa, pn)

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(UPISchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is UPISchemaV1 -> UPISchemaV1.PersistentUPI(
                    pa = this.pa.toString(),
                    pn = this.pn.toString(),
                    mc = this.mc,
                    tid = this.tid,
                    tr = this.tr,
                    tn = this.tn,
                    am = this.am,
                    mam = this.mam,
                    cu = this.cu,
                    url = this.url
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

}
