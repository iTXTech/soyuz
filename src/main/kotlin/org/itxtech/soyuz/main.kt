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
        version = "1.0.0"
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

    private val sessions = Collections.synchronizedSet<SoyuzWebSocketSession>(LinkedHashSet())

    override fun onEnable() {
        SoyuzData.reload()
        if (SoyuzData.token == "pending") {
            SoyuzData.token = (1..20)
                .map { kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
            logger.info("Soyuz Access Token: ${SoyuzData.token}")
        }

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
                    sessions += session

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

                    sessions -= session
                }
            }
        }

        server.start()
        logger.info("iTXTech Soyuz is listening on ws://localhost:${SoyuzData.port}")
    }

    override fun onDisable() {
        server.stop(1000, 3000)
        logger.info("iTXTech Soyuz has been stopped")
    }
}

object SoyuzData : AutoSavePluginConfig("config") {
    var token by value("pending")
    val port by value(9876)
    val enablePushLog by value(true)
}
