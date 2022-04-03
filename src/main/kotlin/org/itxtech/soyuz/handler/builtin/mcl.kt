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
import org.itxtech.mcl.Loader
import org.itxtech.mcl.component.Config
import org.itxtech.soyuz.ReplyMessage
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.HandlerManager
import org.itxtech.soyuz.handler.SoyuzHandler

lateinit var loader: Loader

fun initializeMclHandler() {
    try {
        Class.forName("org.itxtech.mcl.Loader")
        loader = Loader.getInstance()

        HandlerManager.register(MclUpdatePackageHandler())
        HandlerManager.register(MclRemovePackageHandler())
        HandlerManager.register(MclListPackageHandler())
    } catch (e: Exception) {
        Soyuz.logger.warning("Not launched by iTXTech Mirai Console Loader, the MCL Handler feature is disabled")
    }
}

class MclUpdatePackageHandler : SoyuzHandler("mcl-update-package") {
    @Serializable
    data class Info(
        val id: String,
        val type: String? = null,
        val channel: String? = null,
        val version: String? = null,
        val lock: Boolean? = null
    )

    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        val info = Soyuz.json.decodeFromString(Info.serializer(), data)
        val pkg = if (loader.config.hasPackage(info.id)) {
            loader.config.packages.find { p -> p.id == info.id }!!
        } else {
            Config.Package(info.id).apply { loader.config.packages.add(this) }
        }
        pkg.apply {
            if (info.type != null) {
                type = info.type
            }
            if (info.channel != null) {
                channel = info.channel
            }
            if (info.version != null) {
                version = info.version
            }
            if (info.lock != null) {
                versionLocked = info.lock
            }
        }
        loader.saveConfig()
        session.sendText(ReplyMessage(key, "success").toJson())
    }
}

class MclRemovePackageHandler : SoyuzHandler("mcl-remove-package") {
    @Serializable
    data class Info(
        val id: String
    )

    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        val info = Soyuz.json.decodeFromString(Info.serializer(), data)
        if (loader.config.hasPackage(info.id)) {
            loader.config.packages.find { p -> p.id == info.id }?.apply {
                removeFiles()
                loader.config.packages.remove(this)
            }
            session.sendText(ReplyMessage(key, "success").toJson())
        } else {
            session.sendText(ReplyMessage(key, "failed to find package ${info.id}").toJson())
        }
    }
}

class MclListPackageHandler : SoyuzHandler("mcl-list-package") {
    @Serializable
    data class Packages(
        val key: String,
        val packages: List<Package>
    )

    @Serializable
    data class Package(
        val id: String,
        val channel: String,
        val version: String,
        val type: String,
        val versionLocked: Boolean
    ) {
        companion object {
            fun from(pkg: Config.Package): Package {
                return Package(pkg.id, pkg.channel, pkg.version, pkg.type, pkg.versionLocked)
            }
        }
    }

    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        session.sendText(
            Soyuz.json.encodeToString(
                Packages.serializer(), Packages(key,
                    loader.config.packages.map { p -> Package.from(p) })
            )
        )
    }
}
