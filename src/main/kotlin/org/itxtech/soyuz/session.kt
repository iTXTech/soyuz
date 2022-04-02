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
import java.net.InetSocketAddress

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SoyuzWebSocketSession(val session: DefaultWebSocketServerSession) {
    private val remote: InetSocketAddress

    init {
        val origin = session.call.request.origin as io.ktor.server.netty.http1.NettyConnectionPoint
        remote = origin.context.channel().remoteAddress() as InetSocketAddress
    }

    fun connected() {
        Soyuz.logger.info("WebSocket Session connected from ${remote.address}:${remote.port}")
    }

    fun disconnected() {
        Soyuz.logger.info("WebSocket Session disconnected from ${remote.address}:${remote.port}")
    }

    suspend fun handle(text: String) {
        HandlerManager.decode(this, text)
    }

    suspend fun sendText(data: String) {
        session.send(Frame.Text(data))
    }
}
