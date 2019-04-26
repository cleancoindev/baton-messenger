package io.baton.contracts

import io.baton.states.UPI
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

// *****************
// * Contract Code *
// *****************

class UPIContract : Contract {

    companion object {
        val UPI_CONTRACT_ID = UPIContract::class.java.canonicalName
    }

    interface Commands : CommandData {
        class SendPayment : TypeOnlyCommandData(), Commands
        class RequestPayment : TypeOnlyCommandData(), Commands
        class CollectRequest : TypeOnlyCommandData(), Commands

    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.SendPayment -> verifyCreate(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs must be consumed." using (tx.inputStates.isEmpty())
        "Only one out state should be created." using (tx.outputStates.size == 1)
        val output = tx.outputsOfType<UPI>().single()
        "Owner only may sign the Account issue transaction." using (output.pa.owningKey in signers)
    }
}