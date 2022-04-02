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
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.description.PluginDescription
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.SoyuzHandler

class ListPluginHandler : SoyuzHandler("soyuz-list-plugin") {
    @Serializable
    data class PluginList(
        val key: String,
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
                PluginList(key,
                    PluginManager.plugins
                        .map { p -> PluginInfo.fromDescription(p.description) })
            )
        )
    }
}
