package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.manager

class SkipTo : MusicCog(true, true, true) {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["skt"], description = "Skip the current music track.")
    fun skipTo(ctx: Context, where: Int?) {
        val manager = ctx.manager

        val toIndex = where.takeIf { it!! > 0 && it <= manager.scheduler.queue.size }
                ?: return ctx.send("You need to specify the position of the track in the queue that you want to skip to.")

        if (toIndex - 1 == 0) {
            return ctx.send("Use the `${ctx.config.prefix}skip` command to skip single tracks.")
        }

        for (i in 0 until toIndex - 1) {
            manager.scheduler.removeQueueIndex(manager.scheduler.queue, 0)
        }

        manager.scheduler.nextTrack()
        ctx.send("Skipped **${toIndex - 1}** tracks.")
    }
}