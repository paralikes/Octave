package gg.octave.bot.commands.music

import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.music.TrackContext
import gg.octave.bot.utils.Utils
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class NowPlaying : MusicCog {
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    private val totalBlocks = 20

    @Command(aliases = ["nowplaying", "np", "playing"], description = "Shows what's currently playing.")
    fun nowPlaying(ctx: Context) {
        val manager = ctx.manager

        val track = manager.player.playingTrack
        //Reset expire time if np has been called.
        manager.scheduler.queue.clearExpireAsync()

        ctx.send {
            setTitle("Now Playing")
            setDescription("**[${track.info.embedTitle}](${track.info.embedUri})**")
            manager.discordFMTrack?.let {
                val r = buildString {
                    append("Currently streaming music from radio station `${it.station.capitalize()}`")
                    append(", requested by ${it.requesterMention}.")
                }
                addField("Radio", r, false)
            }
            addField(
                "Requester",
                track.getUserData(TrackContext::class.java)?.requesterMention ?: "Unknown.",
                true
            )
            addField(
                "Request Channel",
                track.getUserData(TrackContext::class.java)?.channelMention ?: "Unknown.",
                true
            )
            addBlankField(true)
            addField("Repeating", manager.scheduler.repeatOption.name.toLowerCase().capitalize(), true)
            addField("Shuffle", manager.scheduler.autoShuffle.name.toLowerCase().capitalize(), true)
            addField("Volume", "${manager.player.volume}%", true)
            addField("Bass Boost", manager.dspFilter.bassBoost.name.toLowerCase().capitalize(), true)
            val timeString = if (track.duration == Long.MAX_VALUE) {
                "`Streaming`"
            } else {
                val position = Utils.getTimestamp(track.position)
                val duration = Utils.getTimestamp(track.duration)
                "`[$position / $duration]`"
            }
            addField("Time", timeString, true)
            val percent = track.position.toDouble() / track.duration
            val progress = buildString {
                for (i in 0 until totalBlocks) {
                    if ((percent * (totalBlocks - 1)).toInt() == i) {
                        append("__**\u25AC**__")
                    } else {
                        append("\u2015")
                    }
                }
                append(" **%.1f**%%".format(percent * 100))
            }
            addField("Progress", progress, false)

            if (manager.loops > 5) {
                setFooter("bröther may i have some lööps | You've looped ${manager.loops} times")
            } else {
                setFooter("Use \"${ctx.config.prefix}lyrics\" to see the lyrics of the song!")
            }
        }
    }
}
