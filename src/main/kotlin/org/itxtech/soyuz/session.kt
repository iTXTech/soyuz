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

import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.itxtech.soyuz.handler.HandlerManager
import org.itxtech.soyuz.handler.handleMessage
import java.net.InetSocketAddress

class UnauthorizedSessionException(msg: String) : Exception(msg)

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SoyuzWebSocketSession(private val session: DefaultWebSocketServerSession) {
    private var authorized = false
    val id: String
    var connected = true

    var up = 0
    var down = 0

    init {
        val origin = session.call.request.origin as io.ktor.server.netty.http1.NettyConnectionPoint
        val remote = origin.context.channel().remoteAddress() as InetSocketAddress
        id = "${remote.address}:${remote.port}"
    }

    fun connected() {
        Soyuz.logger.info("WebSocket Session connected from $id")
    }

    fun disconnected() {
        Soyuz.logger.info("WebSocket Session disconnected from $id")
        connected = false
    }

    suspend fun disconnect() {
        session.close()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handle(text: String) {
        down += text.length
        if (authorized) {
            HandlerManager.decode(this, text)
        } else {
            handleMessage(this, text) {
                if (it.key == "authorize") {
                    val msg = Soyuz.json.decodeFromString(AuthorizeMessage.serializer(), text)
                    if (msg.token == SoyuzData.token) {
                        authorized = true
                        sendText(Soyuz.json.encodeToString(ReplyMessage("authorize", "Session has been authorized")))
                        Soyuz.logger.info("Session $id has been authorized")
                        return
                    }
                }
                throw UnauthorizedSessionException("Unauthorized session $id sends a message")
            }
        }
    }

    suspend fun sendText(data: String) {
        up += data.length
        session.send(Frame.Text(data))
    }
}
