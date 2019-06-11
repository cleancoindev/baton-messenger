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

package io.baton

import co.paralleluniverse.fibers.Suspendable
import io.baton.contracts.Chat
import io.baton.contracts.PolicyContract.Companion.POLICY_CONTRACT_ID
import io.baton.contracts.UPIContract
import io.baton.contracts.UPIContract.Companion.UPI_CONTRACT_ID
import io.baton.states.Policy
import io.baton.states.UPI
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant.now
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class SendMessage(private val to: Party, private val userId: String, private val body: String) : FlowLogic<Unit>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new Message.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {
        val stx: SignedTransaction = createMessageStx()
        val otherPartySession = initiateFlow(to)
        progressTracker.nextStep()
        subFlow(FinalityFlow(stx, setOf(otherPartySession), FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun createMessageStx(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txb = TransactionBuilder(notary)
        val me = ourIdentityAndCert.party
        val fromUserId = 21039231.toString()
        val sent = true
        val delivered = false
        val fromMe = true
        val time = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = time.format(formatter)
        val messageNumber = 100.toString()
        txb.addOutputState(Chat.Message(UniqueIdentifier(), body, fromUserId, to, me, userId, sent, delivered, fromMe, formatted, messageNumber), Chat::class.qualifiedName!!)
        txb.addCommand(Chat.SendChatCommand, me.owningKey)
        return serviceHub.signInitialTransaction(txb)
    }

    @InitiatedBy(SendMessage::class)
    class SendChatResponder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data

                }
            }

            val signedTransaction = subFlow(signTransactionFlow)
            return subFlow(ReceiveFinalityFlow(otherSideSession = otherPartySession, expectedTxId = signedTransaction.id))
        }
    }

}



@InitiatingFlow
@StartableByRPC
class SendFile(private val to: Party, private val userId: String, private val attachment: String) : FlowLogic<Unit>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new IOU.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call() {
        val stx: SignedTransaction = createAttachmentStx()
        val otherPartySession = initiateFlow(to)
        progressTracker.nextStep()
        subFlow(FinalityFlow(stx, setOf(otherPartySession), FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun createAttachmentStx(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txb = TransactionBuilder(notary)
        val me = ourIdentityAndCert.party
        val sent = true
        val delivered = false
        val fromMe = true
        val time = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = time.format(formatter)
        txb.addOutputState(Chat.Attachment(UniqueIdentifier(), attachment, to, me, sent, delivered, fromMe, formatted), Chat::class.qualifiedName!!)
        txb.addCommand(Chat.SendFileCommand, me.owningKey)
        return serviceHub.signInitialTransaction(txb)
    }

    @InitiatedBy(SendFile::class)
    class SendFileResponder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data

                }
            }

            val signedTransaction = subFlow(signTransactionFlow)
            return subFlow(ReceiveFinalityFlow(otherSideSession = otherPartySession, expectedTxId = signedTransaction.id))
        }
    }

}


object SendPaymentFlow {
    @StartableByRPC
    @InitiatingFlow
    @Suspendable
    class InitiatePaymentRequest(private val pa: Party,
                                 private val pn: Party,
                                 private val mc: String,
                                 private val tid: String,
                                 private val tr: String,
                                 private val tn: String,
                                 private val am: String,
                                 private val mam: String,
                                 private val cu: String,
                                 private val url: String) : FlowLogic<SignedTransaction>() {

        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new Agreement.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()


        @Suspendable
        override fun call(): SignedTransaction {
            // Obtain a reference to the notary we want to use.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            progressTracker.currentStep = GENERATING_TRANSACTION

            // Generate an unsigned transaction.
            val upiState = UPI(pa, pn, mc, tid, tr, tn, am, mam, cu, url)
            val txCommand = Command(UPIContract.Commands.SendPayment(), upiState.participants.map { it.owningKey })
            progressTracker.currentStep = VERIFYING_TRANSACTION
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(upiState, UPI_CONTRACT_ID)
                    .addCommand(txCommand)

            txBuilder.verify(serviceHub)
            // Sign the transaction.
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)


            progressTracker.currentStep = GATHERING_SIGS
            val otherPartyFlow = initiateFlow(pn)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow), GATHERING_SIGS.childProgressTracker()))
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(InitiatePaymentRequest::class)
    class AcceptPaymentRequest(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a UPI transaction." using (output is UPI)
                    val upi = output as UPI
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}




object SendPolicyFlow {
    @StartableByRPC
    @InitiatingFlow
    @Suspendable
    class InitiatePolicyRequest(private val alice: Party,
                                private val enrico: Party,
                                private val bob: Party,
                                private val policyName: String,
                                private val policyExpirationDate: String,
                                private val policyPassword: String,
                                private val policyId: String) : FlowLogic<SignedTransaction>() {

        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new Agreement.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()


        @Suspendable
        override fun call(): SignedTransaction {
            // Obtain a reference to the notary we want to use.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            progressTracker.currentStep = GENERATING_TRANSACTION

            // Generate an unsigned transaction.
            val policyState = Policy(alice, enrico, bob, policyName, policyExpirationDate, policyPassword, policyId)
            val txCommand = Command(UPIContract.Commands.SendPayment(), policyState.participants.map { it.owningKey })
            progressTracker.currentStep = VERIFYING_TRANSACTION
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(policyState, POLICY_CONTRACT_ID)
                    .addCommand(txCommand)

            txBuilder.verify(serviceHub)
            // Sign the transaction.
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)


            progressTracker.currentStep = GATHERING_SIGS
            val otherPartyFlow = initiateFlow(bob)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow), GATHERING_SIGS.childProgressTracker()))
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(InitiatePolicyRequest::class)
    class AcceptPolicyRequest(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a UPI transaction." using (output is Policy)
                    val policy = output as Policy
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}


