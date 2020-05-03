package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.manager

class Pause : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @Command(description = "Pause or resume the music player.")
    fun pause(ctx: Context) {
        val manager = ctx.manager

        manager.player.isPaused = !manager.player.isPaused

        val message = when (manager.player.isPaused) {
            true -> "Paused the current player."
            false -> "Resumed the current player."
        }

        ctx.send {
            setTitle("Pause")
            setDescription(message)
        }
    }
}