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
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

class Chat : Contract {
    @CordaSerializable
    data class Message(val id: UniqueIdentifier,
                       val body: String,
                       val fromUserId: String,
                       val to: Party,
                       val from: Party,
                       val toUserId: String,
                       val sentReceipt: Boolean?,
                       val deliveredReceipt: Boolean?,
                       val fromMe: Boolean?,
                       val time: String?,
                       val messageNumber: String,
                       override val participants: List<AbstractParty> = listOf(to, from)) : ContractState

    object SendMessageCommand : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        val signers: List<PublicKey> = tx.commandsOfType<SendMessageCommand>().single().signers
        val message: Message = tx.outputsOfType<Message>().single()
        requireThat {
            "the chat message is signed by the claimed sender" using (message.from.owningKey in signers)
        }
    }

   // override fun supportedSchemas() = listOf(ChatSchemaV1)


    object ChatSchema

    object ChatSchemaV1 : MappedSchema(ChatSchema.javaClass, 1, listOf(PersistentMessages::class.java)) {
        @Entity
        @Table(name = "messages")
        class PersistentMessages(
                @Column(name = "id")
                var id: String = "",
                @Column(name = "body")
                var body: String = "",
                @Column(name = "fromUserId")
                var fromUserId: String = "",
                @Column(name = "to")
                var to: String = "",
                @Column(name = "from")
                var from: String = "",
                @Column(name = "toUserId")
                var toUserId: String = "",
                @Column(name = "sentReceipt")
                var sentReceipt: String = "",
                @Column(name = "deliveredReceipt")
                var deliveredReceipt: String = "",
                @Column(name = "fromMe")
                var fromMe: String = "",
                @Column(name = "time")
                var time: String = "",
                @Column(name = "messageNumber")
                var messageNumber: String = ""
        ) : PersistentState()
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
