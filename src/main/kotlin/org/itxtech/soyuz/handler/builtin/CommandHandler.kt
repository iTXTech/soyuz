/*
 *
 * iTXTech Soyuz
 *
 * Copyright (C) 2022 iTX Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author PeratX
 * @website https://github.com/iTXTech/soyuz
 *
 */

package org.itxtech.soyuz.handler.builtin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.SoyuzHandler
import kotlin.coroutines.CoroutineContext

class CommandHandler : SoyuzHandler("soyuz-run-command") {
    @Serializable
    data class RunCommand(
        val command: String
    )

    @Serializable
    data class CommandResult(
        val key: String,
        val result: String,
        val output: ArrayList<String>
    )

    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        val cmd = Soyuz.json.decodeFromString(RunCommand.serializer(), data)
        Soyuz.logger.info("Session ${session.id} executes a command: ${cmd.command}")
        val sender = SoyuzCommandSender(session.id)
        val result = CommandManager.executeCommand(sender, PlainText(cmd.command))
        session.sendText(
            Soyuz.json.encodeToString(
                CommandResult.serializer(),
                CommandResult(key, result.javaClass.simpleName, sender.messages)
            )
        )
        sender.messages.clear()
    }
}

class SoyuzCommandSender(id: String) : CommandSender {
    private val NAME: String = "SoyuzCommandSender[$id]"

    override val bot: Nothing? get() = null
    override val subject: Nothing? get() = null
    override val user: Nothing? get() = null
    override val name: String get() = NAME
    override fun toString(): String = NAME

    override val permitteeId: AbstractPermitteeId.Console = AbstractPermitteeId.Console

    override val coroutineContext: CoroutineContext by lazy { Soyuz.coroutineContext }

    val messages = arrayListOf<String>()

    override suspend fun sendMessage(message: Message): Nothing? {
        ConsoleCommandSender.sendMessage(message)
        messages.add(message.contentToString())
        return null
    }

    override suspend fun sendMessage(message: String): Nothing? {
        ConsoleCommandSender.sendMessage(message)
        messages.add(message)
        return null
    }
}
