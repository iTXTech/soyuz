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
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzData
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.SoyuzHandler

@OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)
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
}
