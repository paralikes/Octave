package xyz.gnarbot.gnar.utils.extensions

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE

open class MusicCog(private val sameChannel: Boolean, private val requirePlayingTrack: Boolean, private val requirePlayer: Boolean) : Cog {
    fun check(ctx: Context): Boolean {
        val manager = ctx.bot.players.getExisting(ctx.guild)

        if (manager == null) {
            ctx.send("There's no music player in this server.\n$PLAY_MESSAGE")
            return false
        }

        val botChannel = ctx.selfMember!!.voiceState?.channel

        if (requirePlayer && botChannel == null) {
            ctx.send("The bot is not currently in a voice channel.\n$PLAY_MESSAGE")
            return false
        }

        if (sameChannel && ctx.voiceChannel != botChannel) {
            ctx.send("You're not in the same channel as the context.bot.")
            return false
        }

        if (requirePlayingTrack && manager.player.playingTrack == null) {
            ctx.send("The player is not playing anything.")
            return false
        }

        return true
    }
}