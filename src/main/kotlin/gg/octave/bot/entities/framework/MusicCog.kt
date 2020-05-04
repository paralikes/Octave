package gg.octave.bot.entities.framework

import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.PLAY_MESSAGE
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.extensions.voiceChannel
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.entities.Cog

interface MusicCog : Cog {
    fun sameChannel() = false
    fun requirePlayingTrack() = false
    fun requirePlayer() = false

    fun check(ctx: Context): Boolean {
        val manager = Launcher.players.getExisting(ctx.guild)

        if (manager == null) {
            ctx.send("There's no music player in this server.\n$PLAY_MESSAGE")
            return false
        }

        val botChannel = ctx.selfMember!!.voiceState?.channel

        if (requirePlayer() && botChannel == null) {
            ctx.send("The bot is not currently in a voice channel.\n$PLAY_MESSAGE")
            return false
        }

        if (sameChannel() && ctx.voiceChannel != botChannel) {
            ctx.send("You're not in the same channel as the bot.")
            return false
        }

        if (requirePlayingTrack() && manager.player.playingTrack == null) {
            ctx.send("The player is not playing anything.")
            return false
        }

        return true
    }
}
