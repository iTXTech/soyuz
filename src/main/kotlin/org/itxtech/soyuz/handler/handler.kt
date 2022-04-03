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

package org.itxtech.soyuz.handler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.itxtech.soyuz.BaseMessage
import org.itxtech.soyuz.ReplyMessage
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.builtin.CommandHandler
import org.itxtech.soyuz.handler.builtin.ListPluginHandler
import org.itxtech.soyuz.handler.builtin.MiraiInfoHandler
import org.itxtech.soyuz.handler.builtin.PushLogHandler

class HandlerAlreadyExistsException(msg: String) : Exception(msg)
class InvalidSoyuzMessageException(msg: String) : Exception(msg)

object HandlerManager {
    private val handlers = hashMapOf<String, SoyuzHandler>()

    init {
        register(ListPluginHandler())
        register(MiraiInfoHandler())
        register(CommandHandler())
        register(PushLogHandler())
    }

    fun register(handler: SoyuzHandler): HandlerManager {
        if (handlers.containsKey(handler.key)) {
            throw HandlerAlreadyExistsException("Handler ${handler.key} has been already registered")
        }
        handlers[handler.key] = handler
        return this
    }

    suspend fun decode(session: SoyuzWebSocketSession, text: String) {
        handleMessage(session, text) {
            if (handlers.containsKey(it.key)) {
                val handler = handlers[it.key]!!
                handler.handle(session, text)
            } else {
                throw InvalidSoyuzMessageException("Invalid message key ${it.key}")
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun handleMessage(
    session: SoyuzWebSocketSession,
    text: String,
    block: (it: BaseMessage) -> Unit
): Boolean {
    try {
        block(Soyuz.json.decodeFromString(BaseMessage.serializer(), text))
        return true
    } catch (e: Throwable) {
        session.sendText(Soyuz.json.encodeToString(ReplyMessage.error(e.message ?: "Error with no message")))
        Soyuz.logger.error(e)
    }
    return false
}

abstract class SoyuzHandler(val key: String) {
    abstract suspend fun handle(session: SoyuzWebSocketSession, data: String)
}
