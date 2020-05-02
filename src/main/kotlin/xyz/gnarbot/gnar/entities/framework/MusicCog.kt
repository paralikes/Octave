package xyz.gnarbot.gnar.entities.framework

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE
import xyz.gnarbot.gnar.utils.extensions.selfMember
import xyz.gnarbot.gnar.utils.extensions.voiceChannel

open class MusicCog(private val sameChannel: Boolean, private val requirePlayingTrack: Boolean, private val requirePlayer: Boolean) : Cog {
    fun check(ctx: Context): Boolean {
        val manager = Launcher.players.getExisting(ctx.guild)

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
            ctx.send("You're not in the same channel as the bot.")
            return false
        }

        if (requirePlayingTrack && manager.player.playingTrack == null) {
            ctx.send("The player is not playing anything.")
            return false
        }

        return true
    }
}
