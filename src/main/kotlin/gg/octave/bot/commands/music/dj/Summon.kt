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
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(description = "Move the bot to another channel.")
    fun summon(ctx: Context, @Greedy channel: VoiceChannel?) {
//        val vc = channel
//            ?: ctx.voiceChannel
//            ?: return ctx.send("That's not a valid music channel. " +
//                "You can join a VC and run this command without arguments to make it join that channel.")
//
//        if (vc == ctx.selfMember!!.voiceState?.channel) {
//            return ctx.send("That's the same channel as I'm currently in.")
//        }
//
//        val data = ctx.data
//
//        if (data.music.channels.isNotEmpty()) {
//            if (vc.id !in data.music.channels) {
//                return ctx.send("Can not join `${vc.name}`, it isn't one of the designated music channels.")
//            }
//        }
        val musicManager = try {
            Launcher.players.get(ctx.guild!!)
        } catch (e: MusicLimitException) {
            return e.sendToContext(ctx)
        }

        if (ctx.guild!!.audioManager.connectedChannel != null) {
            musicManager.moveAudioConnection(ctx.voiceChannel!!)
        } else {
            musicManager.openAudioConnection(ctx.voiceChannel!!, ctx)
        }
    }
}
