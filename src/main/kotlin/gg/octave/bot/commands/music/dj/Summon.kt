package gg.octave.bot.commands.music.dj

import gg.octave.bot.Launcher
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.MusicLimitException
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.extensions.voiceChannel
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import net.dv8tion.jda.api.entities.VoiceChannel

class Summon : MusicCog {
    override fun requireManager() = false

    @DJ
    @Command(description = "Connects, or moves the bot to a voice channel.")
    fun summon(ctx: Context, @Greedy channel: VoiceChannel?) {
        val musicManager = try {
            Launcher.players.get(ctx.guild!!)
        } catch (e: MusicLimitException) {
            return e.sendToContext(ctx)
        }

        val targetChannel = channel ?: ctx.voiceChannel!!

        if (ctx.guild!!.audioManager.connectedChannel != null) {
            musicManager.moveAudioConnection(targetChannel)
        } else {
            musicManager.openAudioConnection(targetChannel, ctx)
        }
    }
}
