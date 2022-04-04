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

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import java.time.Duration
import java.util.*

object Soyuz : KotlinPlugin(
    JvmPluginDescription(
        id = "org.itxtech.soyuz",
        name = "Soyuz",
        version = "1.0.0-beta.1"
    ) {
        author("PeratX")
        info("The Websocket API Server for Mirai Console")
    }
) {
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
    }

    private lateinit var server: ApplicationEngine

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val sessions: MutableMap<String, SoyuzWebSocketSession> = Collections.synchronizedMap(LinkedHashMap())

    fun generateRandomString(len: Int) = (1..len)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

    override fun onEnable() {
        SoyuzData.reload()
        if (SoyuzData.token == "pending") {
            SoyuzData.token = generateRandomString(20)
            logger.info("Soyuz Access Token: ${SoyuzData.token}")
        }

        if (SoyuzData.enablePushLog) {
            try {
                Class.forName("net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminalKt")
            } catch (e: Exception) {
                SoyuzData.enablePushLog = false
                logger.warning("Push Log is only available when running Mirai Console Terminal")
            }
        }

        SoyuzCommand.register()

        server = embeddedServer(Netty, port = SoyuzData.port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            routing {
                webSocket("/") {
                    val session = SoyuzWebSocketSession(this)
                    sessions[session.id] = session

                    session.connected()

                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                session.handle(frame.readText())
                            }
                            else -> {}
                        }
                    }

                    session.disconnected()

                    sessions.remove(session.id)
                }
            }
        }

        server.start()
        logger.info("iTXTech Soyuz is listening on ws://localhost:${SoyuzData.port}")
    }

    override fun onDisable() {
        SoyuzCommand.unregister()
        server.stop(1000, 3000)
        logger.info("iTXTech Soyuz has been stopped")
    }
}

object SoyuzData : AutoSavePluginConfig("config") {
    var token by value("pending")
    var port by value(9876)
    var enablePushLog by value(true)
}
