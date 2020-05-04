/*
 * Copyright (C) 2016-2020 David Alejandro Rubio Escares / Kodehawa
 *
 *  Mantaro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Mantaro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro.  If not, see http://www.gnu.org/licenses/
 *
 */

//Original class in Mantaro made by natanbc
package gg.octave.bot.entities.sharding

import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnegative
import javax.annotation.Nonnull

class BucketedController(@Nonnegative bucketFactor: Int, homeGuildId: Long) : SessionControllerAdapter() {
    private val shardControllers: Array<SessionController?>

    constructor(homeGuildId: Long) : this(16, homeGuildId)

    override fun appendSession(@Nonnull node: SessionConnectNode) {
        controllerFor(node)!!.appendSession(node)
    }

    override fun removeSession(@Nonnull node: SessionConnectNode) {
        controllerFor(node)!!.removeSession(node)
    }

    @Nonnull
    @CheckReturnValue
    private fun controllerFor(@Nonnull node: SessionConnectNode): SessionController? {
        return shardControllers[node.shardInfo.shardId % shardControllers.size]
    }

    init {
        require(bucketFactor >= 1) { "Bucket factor must be at least 1" }
        shardControllers = arrayOfNulls(bucketFactor)
        for (i in 0 until bucketFactor) {
            shardControllers[i] = PrioritizingSessionController(homeGuildId)
        }
    }
}