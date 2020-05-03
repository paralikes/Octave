package gg.octave.bot.commands.music

import com.jagrosh.jdautilities.paginator
import gg.octave.bot.Launcher
import gg.octave.bot.music.TrackContext
import gg.octave.bot.utils.PlaylistUtils
import gg.octave.bot.utils.Utils
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.selfMember
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog

class Queue : Cog {
    @Command(aliases = ["q"], description = "Shows the current queue.")
    fun queue(ctx: Context) {
        val manager = Launcher.players.getExisting(ctx.guild)
            ?: return ctx.send("There's no music player in this guild.\n$PLAY_MESSAGE")

        val queue = manager.scheduler.queue
        var queueLength = 0L

        ctx.textChannel?.let {
            Launcher.eventWaiter.paginator {
                setUser(ctx.author)
                title { "Music Queue" }
                color { ctx.selfMember?.color }
                empty { "**Empty queue.** Add some music with `${ctx.config.prefix}play url|YT search`." }
                finally { message -> message!!.delete().queue() }

                for (track in queue) {
                    val decodedTrack = PlaylistUtils.toAudioTrack(track)

                    entry {
                        buildString {
                            decodedTrack.getUserData(TrackContext::class.java)?.requester?.let { m ->
                                ctx.guild!!.getMemberById(m)?.let { member ->
                                    append(member.asMention)
                                    append(' ')
                                }
                            }

                            append("`[").append(Utils.getTimestamp(decodedTrack.duration)).append("]` __[")
                            append(decodedTrack.info.embedTitle)
                            append("](").append(decodedTrack.info.embedUri).append(")__")
                        }
                    }

                    queueLength += decodedTrack.duration
                }

                field("Now Playing", false) {
                    val track = manager.player.playingTrack
                    if (track == null) {
                        "Nothing"
                    } else {
                        "**[${track.info.embedTitle}](${track.info.uri})**"
                    }
                }

                manager.discordFMTrack?.let {
                    field("Radio") {
                        val member = ctx.guild!!.getMemberById(it.requester)
                        buildString {
                            append("Currently streaming music from radio station `${it.station.capitalize()}`")
                            member?.let {
                                append(", requested by ${member.asMention}")
                            }
                            append(". When the queue is empty, random tracks from the station will be added.")
                        }
                    }
                }

                field("Entries", true) { queue.size }
                field("Total Duration", true) { Utils.getTimestamp(queueLength) }
                field("Repeating", true) { manager.scheduler.repeatOption.name.toLowerCase().capitalize() }
            }.display(it)
        }
    }
}
