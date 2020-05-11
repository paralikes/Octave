package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.PlaylistUtils
import gg.octave.bot.utils.extensions.manager
import gg.octave.bot.utils.extensions.move
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Move : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @Command(description = "Moves tracks within the queue.")
    fun move(ctx: Context, trackIndex: Int, toIndex: Int) {
        val manager = ctx.manager
        val queue = manager.scheduler.queue

        if (queue.size < 2) {
            return ctx.send("There are no tracks in the queue to move.")
        }

        val realIndex = trackIndex.takeIf { it in 1..queue.size }?.minus(1)
            ?: return ctx.send("`trackIndex` needs to be ≥ 1, and ≤ ${queue.size}.")

        val realTo = toIndex.takeIf { it in 1..queue.size && it != trackIndex }?.minus(1)
            ?: return ctx.send("`trackIndex` needs to be ≥ 1, ≤ ${queue.size}, and must not be the same as the index of the track you're moving.")

        val moved = queue.move(realIndex, realTo)
        val track = PlaylistUtils.toAudioTrack(moved)

        ctx.send("Moved **${track.info.title}** to position **$toIndex** in the queue.")
    }
}
