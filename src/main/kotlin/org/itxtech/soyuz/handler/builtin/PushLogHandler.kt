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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzData
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.SoyuzHandler

@OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)
@Suppress("INVISIBLE_REFERENCE")
class PushLogHandler : SoyuzHandler("soyuz-push-log") {
    @Serializable
    data class PushLog(
        val enable: Boolean
    )

    @Serializable
    data class Log(
        val key: String, val msg: String
    )

    val enabledSession = hashMapOf<String, SoyuzWebSocketSession>()

    @OptIn(DelicateCoroutinesApi::class)
    val context = newSingleThreadContext("PushLogHandler Message")

    init {
        if (SoyuzData.enablePushLog) {
            try {
                val terminal = MiraiConsoleImplementation.getInstance().origin as MiraiConsoleImplementationTerminal
                val serviceField = terminal::class.java.getDeclaredField("logService")
                serviceField.isAccessible = true
                serviceField.set(
                    terminal, SoyuzLogService(
                        this,
                        serviceField.get(terminal) as net.mamoe.mirai.console.terminal.LoggingService
                    )
                )

                val senderField = terminal::class.java.getDeclaredField("consoleCommandSender")
                senderField.isAccessible = true
                senderField.set(
                    terminal, SoyuzConsoleCommandSender(
                        this,
                        senderField.get(terminal) as MiraiConsoleImplementation.ConsoleCommandSenderImpl
                    )
                )
            } catch (e: Exception) {
                Soyuz.logger.error(e)
            }
        }
    }

    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        if (SoyuzData.enablePushLog) {
            val en = Soyuz.json.decodeFromString(PushLog.serializer(), data)
            if (en.enable) {
                enabledSession[session.id] = session
            } else {
                enabledSession.remove(session.id)
            }
        }
    }

    fun sendLine(line: String) {
        Soyuz.launch(context) {
            val it = enabledSession.iterator()
            while (it.hasNext()) {
                val session = it.next()
                if (!session.value.connected) {
                    it.remove()
                } else {
                    session.value.sendText(Soyuz.json.encodeToString(Log.serializer(), Log(key, line)))
                }
            }
        }
    }
}

@Suppress(
    "SEALED_INHERITOR_IN_DIFFERENT_MODULE", "SEALED_INHERITOR_IN_DIFFERENT_PACKAGE",
    "INVISIBLE_MEMBER", "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_ERROR", "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER", "NOTHING_TO_OVERRIDE"
)
internal class SoyuzLogService(
    private val pushLogHandler: PushLogHandler,
    private val base: net.mamoe.mirai.console.terminal.LoggingService
) :
    net.mamoe.mirai.console.terminal.LoggingService() {
    override fun `pushLine$mirai_console_terminal`(line: String) {
        base.pushLine(line)
        pushLogHandler.sendLine(line)
    }
}

@ConsoleFrontEndImplementation
internal class SoyuzConsoleCommandSender(
    private val pushLogHandler: PushLogHandler,
    private val base: MiraiConsoleImplementation.ConsoleCommandSenderImpl
) :
    MiraiConsoleImplementation.ConsoleCommandSenderImpl {
    override suspend fun sendMessage(message: String) {
        base.sendMessage(message)
        pushLogHandler.sendLine(message)
    }

    override suspend fun sendMessage(message: Message) {
        return sendMessage(message.toString())
    }
}
