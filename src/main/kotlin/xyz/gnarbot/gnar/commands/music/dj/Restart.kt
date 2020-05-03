package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.commands.music.embedTitle
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.manager

class Restart : MusicCog(true, false, true) {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["replay"], description = "Restart the current song.")
    fun restart(ctx: Context) {
        val manager = ctx.manager

        val track = manager.player.playingTrack ?: manager.scheduler.lastTrack
        ?: return ctx.send("No track has been previously started.")

        ctx.send("Restarting track: `${track.info.embedTitle}`.")
        manager.player.playTrack(track.makeClone())
    }
}