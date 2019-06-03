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

package io.baton.contracts

import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class Chat : Contract {
    @CordaSerializable
    data class Message(val messageId: UniqueIdentifier,
                       val message: String,
                      // val fromUserId: User,
                       val to: Party,
                       val from: Party,
                     //  val toUserId: User,
                       val sentReceipt: Boolean?,
                       val deliveredReceipt: Boolean?,
                       val fromMe: Boolean?,
                       val time: String?,
                       override val participants: List<AbstractParty> = listOf(to, from)) : ContractState

    object SendChatCommand : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        val signers: List<PublicKey> = tx.commandsOfType<SendChatCommand>().single().signers
        val message: Message = tx.outputsOfType<Message>().single()
        requireThat {
            "the chat message is signed by the claimed sender" using (message.from.owningKey in signers)
        }
    }

    data class Attachment(val attachmentId: UniqueIdentifier,
                          val attachment: String,
                          val to: Party,
                          val from: Party,
                          val sentReceipt: Boolean?,
                          val deliveredReceipt: Boolean?,
                          val fromMe: Boolean?,
                          val time: String?,
                          override val participants: List<AbstractParty> = listOf(to, from)) : ContractState

    object SendFileCommand : TypeOnlyCommandData()

    fun verifyAttachment(tx: LedgerTransaction) {
        val signers: List<PublicKey> = tx.commandsOfType<SendFileCommand>().single().signers
        val attachment: Attachment = tx.outputsOfType<Attachment>().single()
        requireThat {
            "the file is signed by the claimed sender" using (attachment.from.owningKey in signers)

        }
    }

    @CordaSerializable
    data class Baton(val batonId: UniqueIdentifier,
                     val name: String
    )
}
