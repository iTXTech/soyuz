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

package org.itxtech.soyuz

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.description.PluginDescription

class HandlerAlreadyExistsException(msg: String) : Exception(msg)
class InvalidSoyuzMessageException(msg: String) : Exception(msg)

@Serializable
data class BaseMessage(
    val key: String
)

@Serializable
data class ReplyMessage(
    val key: String,
    val msg: String
) {
    companion object {
        fun error(msg: String): ReplyMessage {
            return ReplyMessage("error", msg)
        }
    }
}

object HandlerManager {
    private val handlers = hashMapOf<String, SoyuzHandler>()

    init {
        register(ListPluginHandler())
    }

    fun register(handler: SoyuzHandler): HandlerManager {
        if (handlers.containsKey(handler.key)) {
            throw HandlerAlreadyExistsException("Handler ${handler.key} has been already registered")
        }
        handlers[handler.key] = handler
        return this
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun decode(session: SoyuzWebSocketSession, text: String) {
        try {
            val base = Soyuz.json.decodeFromString<BaseMessage>(text)
            if (handlers.containsKey(base.key)) {
                val handler = handlers[base.key]!!
                handler.handle(session, text)
            } else {
                throw InvalidSoyuzMessageException("Invalid message key ${base.key}")
            }
        } catch (e: Throwable) {
            session.session.send(
                Frame.Text(
                    Soyuz.json.encodeToString(
                        ReplyMessage.error(e.message ?: "Error with no message")
                    )
                )
            )
            Soyuz.logger.error(e)
        }
    }
}

abstract class SoyuzHandler(val key: String) {
    abstract suspend fun handle(session: SoyuzWebSocketSession, data: String)
}

class ListPluginHandler : SoyuzHandler("soyuz-list-plugin") {
    @Serializable
    data class PluginList(
        val list: List<PluginInfo>
    )

    @Serializable
    data class PluginInfo(
        val name: String,
        val version: String,
        val info: String?,
        val author: String?
    ) {
        companion object {
            fun fromDescription(desc: PluginDescription): PluginInfo {
                return PluginInfo(desc.name, desc.version.toString(), desc.info, desc.author)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        session.sendText(
            Soyuz.json.encodeToString(
                PluginList(PluginManager.plugins
                    .map { p -> PluginInfo.fromDescription(p.description) })
            )
        )
    }
}
