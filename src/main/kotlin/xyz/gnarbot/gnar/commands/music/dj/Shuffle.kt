package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.manager

class Shuffle : MusicCog(true, false, true) {
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