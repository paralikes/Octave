package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import net.dv8tion.jda.api.entities.VoiceChannel
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.selfMember
import xyz.gnarbot.gnar.utils.extensions.voiceChannel

class Summon : MusicCog {
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(description = "Move the bot to another channel.")
    fun summon(ctx: Context, channel: VoiceChannel?) {
        val vc = channel
                ?: ctx.voiceChannel
                ?: return ctx.send("That's not a valid music channel. " +
                        "You can join a VC and run this command without arguments to make it join that channel.")

        if (vc == ctx.selfMember!!.voiceState?.channel) {
            return ctx.send("That's the same channel as I'm currently in.")
        }

        val data = ctx.data

        if (data.music.channels.isNotEmpty()) {
            if (vc.id !in data.music.channels) {
                return ctx.send("Can not join `${vc.name}`, it isn't one of the designated music channels.")
            }
        }

        ctx.guild!!.audioManager.openAudioConnection(vc)
    }
}