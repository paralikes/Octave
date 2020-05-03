package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.manager

class Skip : MusicCog(true, true, true) {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["sk", "s"], description = "Skips the current track.")
    fun skip(ctx: Context) {
        ctx.manager.scheduler.nextTrack()
        ctx.send("Skipped current track.")
    }
}