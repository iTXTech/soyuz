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

import kotlinx.serialization.Serializable

@Serializable
data class BaseMessage(
    val key: String
)

@Serializable
data class ReplyMessage(
    val key: String, val msg: String
) {
    companion object {
        fun error(msg: String): ReplyMessage {
            return ReplyMessage("error", msg)
        }
    }

    fun toJson(): String = Soyuz.json.encodeToString(serializer(), this)
}

@Serializable
data class AuthorizeMessage(
    val token: String
)
