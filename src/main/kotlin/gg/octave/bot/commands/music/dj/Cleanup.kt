package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.TrackContext
import gg.octave.bot.utils.PlaylistUtils
import gg.octave.bot.utils.extensions.DEFAULT_SUBCOMMAND
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.SubCommand
import net.dv8tion.jda.api.entities.Member
import java.time.Duration

class Cleanup : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["cu"], description = "Clear songs based on a specific user, duplicates, or if a user left")
    fun cleanup(ctx: Context, @Greedy member: Member?) {
        if (member == null) {
            return DEFAULT_SUBCOMMAND(ctx)
        }

        val oldSize = ctx.manager.scheduler.queue.size

        val predicate: (String) -> Boolean = {
            val track = PlaylistUtils.toAudioTrack(it)
            track.getUserData(TrackContext::class.java)?.requester == member.idLong
        }

        ctx.manager.scheduler.queue.removeIf(predicate)
        val newSize = ctx.manager.scheduler.queue.size

        val removed = oldSize - newSize
        if (removed == 0) {
            return ctx.send("There are no songs to clear.")
        }

        ctx.send("Removed $removed songs from the user ${member.user.asTag}.")
    }

    @SubCommand(aliases = ["d", "dupes"])
    fun left(ctx: Context) {
        val oldSize = ctx.manager.scheduler.queue.size

        // Return Boolean: True if track should be removed
        val predicate: (String) -> Boolean = check@{
            val track = PlaylistUtils.toAudioTrack(it)
            
            val req = track.getUserData(TrackContext::class.java)?.let { m -> ctx.guild?.getMemberById(m.requester) }
                ?: return@check true

            return@check req.voiceState?.channel?.idLong != ctx.guild!!.selfMember.voiceState?.channel?.idLong
        }

        ctx.manager.scheduler.queue.removeIf(predicate)
        val newSize = ctx.manager.scheduler.queue.size
        val removed = oldSize - newSize
        if (removed == 0) {
            return ctx.send("There are no songs to clear.")
        }

        ctx.send("Removed $removed songs from users no longer in the voice channel.")
    }

    @SubCommand
    fun duplicates(ctx: Context) {
        val oldSize = ctx.manager.scheduler.queue.size

        val tracks = mutableSetOf<String>()
        // Return Boolean: True if track should be removed (could not add to set: already exists).
        val predicate: (String) -> Boolean = {
            val track = PlaylistUtils.toAudioTrack(it)
            !tracks.add(track.identifier)
        }

        ctx.manager.scheduler.queue.removeIf(predicate)
        val newSize = ctx.manager.scheduler.queue.size

        val removed = oldSize - newSize
        if (removed == 0) {
            return ctx.send("There are no duplicate songs to clear.")
        }

    }

    @SubCommand(aliases = ["longerthan", "duration", "time"])
    fun exceeds(ctx: Context, duration: Duration) {
        val oldSize = ctx.manager.scheduler.queue.size

        ctx.manager.scheduler.queue.removeIf { PlaylistUtils.toAudioTrack(it).duration > duration.toMillis() }
        val newSize = ctx.manager.scheduler.queue.size

        val removed = oldSize - newSize
        if (removed == 0) {
            return ctx.send("There are no songs to clear.")
        }

        //TODO make this smarter lol
        ctx.send("Removed $removed songs longer than ${duration.toMinutes()} minutes.")
    }
}