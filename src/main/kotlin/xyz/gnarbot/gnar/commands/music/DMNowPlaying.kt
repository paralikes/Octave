package xyz.gnarbot.gnar.commands.music

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import net.dv8tion.jda.api.EmbedBuilder
import xyz.gnarbot.gnar.utils.Utils
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.manager

class DMNowPlaying : MusicCog(false, true, true) {
    private val totalBlocks = 20

    @Command(description = "DMs you the now playing message with the song URL.")
    fun dmnp(ctx: Context) {
        val manager = ctx.manager

        val track = manager.player.playingTrack
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

        ctx.author.openPrivateChannel().flatMap { channel ->
            channel.sendMessage(EmbedBuilder().apply {
                setDescription(
                    "**[${track.info.embedTitle}](${track.info.embedUri})**\n" +
                        "Track URL: ${track.info.uri}"
                )
                addField("Repeating", manager.scheduler.repeatOption.name.toLowerCase().capitalize(), true)
                addField("Bass Boost", manager.dspFilter.bassBoost.name.toLowerCase().capitalize(), true)
                val timeString = if (track.duration == Long.MAX_VALUE) {
                    "`Streaming`"
                } else {
                    val position = Utils.getTimestamp(track.position)
                    val duration = Utils.getTimestamp(track.duration)
                    "`[$position / $duration]`"
                }
                addField("Time", timeString, true)
                addField("Progress", progress, false)
            }.build())
        }.queue()
    }
}
