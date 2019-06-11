/**
 *   Copyright 2020, Dapps Incorporated.
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

package io.baton.contracts

import io.baton.states.Policy
import io.baton.states.UPI
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

// *****************
// * Contract Code *
// *****************

class PolicyContract : Contract {

    companion object {
        val POLICY_CONTRACT_ID = PolicyContract::class.java.canonicalName
    }

    interface Commands : CommandData {
        class SendPolicy : TypeOnlyCommandData(), Commands
        class RequestPayment : TypeOnlyCommandData(), Commands
        class CollectRequest : TypeOnlyCommandData(), Commands

    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.SendPolicy -> verifyCreate(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs must be consumed." using (tx.inputStates.isEmpty())
        "Only one out state should be created." using (tx.outputStates.size == 1)
        val output = tx.outputsOfType<Policy>().single()
        "Owner only may sign the Account issue transaction." using (output.alice.owningKey in signers)
    }
}