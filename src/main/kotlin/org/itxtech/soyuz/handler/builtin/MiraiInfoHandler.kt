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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.SoyuzHandler

class MiraiInfoHandler : SoyuzHandler("soyuz-mirai-info") {
    @Serializable
    data class MiraiInfo(
        val key: String,
        val version: String,
        val buildDate: Long
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        session.sendText(
            Soyuz.json.encodeToString(
                MiraiInfo(
                    key,
                    net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.versionConst,
                    net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.buildDate.epochSecond
                )
            )
        )
    }
}
