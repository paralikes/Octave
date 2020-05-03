package gg.octave.bot.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import gg.octave.bot.commands.music.PLAY_MESSAGE
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.utils.extensions.manager
import gg.octave.bot.utils.extensions.voiceChannel

class Resume : Cog {
    @DJ
    @CheckVoiceState
    @Command(description = "Resume the music queue.")
    fun resume(ctx: Context) {
        val manager = ctx.manager
        val scheduler = manager.scheduler

        if(ctx.voiceChannel == null) {
            return ctx.send("You need to be in a voice channel.")
        }

        if (scheduler.queue.isEmpty()) {
            return ctx.send("The queue is empty.\n$PLAY_MESSAGE")
        }

        if (scheduler.lastTrack != null) {
            return ctx.send("There's nothing to resume as the player has been active here!")
        }

        //Reset expire time if play has been called.
        manager.scheduler.queue.clearExpire()

        //Poll next from queue and force that track to play.
        manager.openAudioConnection(ctx.voiceChannel!!, ctx)
        scheduler.nextTrack()

        ctx.send("Queue has been resumed.")
    }
}