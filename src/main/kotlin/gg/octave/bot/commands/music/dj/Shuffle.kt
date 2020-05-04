package gg.octave.bot.commands.music.dj

import gg.octave.bot.commands.music.PLAY_MESSAGE
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Shuffle : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(description = "Shuffles the queue order around.")
    fun shuffle(ctx: Context) {
        val manager = ctx.manager

        if (manager.scheduler.queue.isEmpty()) {
            return ctx.send("The queue is empty.\n$PLAY_MESSAGE")
        }

        manager.scheduler.shuffle()
        ctx.send("Player has been shuffled")
    }
}