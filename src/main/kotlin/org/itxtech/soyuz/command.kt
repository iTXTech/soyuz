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

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.text.StringCharacterIterator

@OptIn(ConsoleExperimentalApi::class)
object SoyuzCommand : CompositeCommand(
    Soyuz,
    primaryName = "soyuz",
    description = "设置 iTXTech Soyuz"
) {
    @SubCommand
    @Description("设置或重新生成 Access Token")
    suspend fun CommandSender.token(token: String = "") {
        SoyuzData.token = if (token == "") {
            Soyuz.generateRandomString(20)
        } else {
            token
        }
        sendMessage("New Soyuz Access Token: ${SoyuzData.token}")
    }

    @SubCommand
    @Description("断开指定连接")
    suspend fun CommandSender.disconnect(@Name("连接ID") id: String) {
        if (Soyuz.sessions.containsKey(id)) {
            Soyuz.sessions[id]!!.disconnect()
            sendMessage("Session $id has been disconnect")
        } else {
            sendMessage("Session $id does not exist")
        }
    }

    @SubCommand
    @Description("断开所有连接")
    suspend fun CommandSender.disconnectAll() {
        Soyuz.sessions.forEach {
            it.value.disconnect()
            sendMessage("Session ${it.value.id} has been disconnect")
        }
    }

    @SubCommand
    @Description("列出所有连接")
    suspend fun CommandSender.list() {
        Soyuz.sessions.values.forEach {
            sendMessage("Session ${it.id}  Received: ${humanReadableSize(it.down)}  Sent: ${humanReadableSize(it.up)}")
        }
    }

    private fun humanReadableSize(bytes: Int): String {
        val absB = if (bytes == Int.MIN_VALUE) Int.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "$bytes B"
        }
        var value = absB
        val ci = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= Integer.signum(bytes)
        return String.format("%.2f %cB", value / 1024.0, ci.current())
    }
}
