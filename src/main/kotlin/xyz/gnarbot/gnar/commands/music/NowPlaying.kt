package xyz.gnarbot.gnar.commands.music

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.utils.Utils
import xyz.gnarbot.gnar.utils.desc
import xyz.gnarbot.gnar.utils.extensions.MusicCog
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.manager
import xyz.gnarbot.gnar.utils.field

class NowPlaying: MusicCog(false, true, true) {
    private val totalBlocks = 20

    @Command(aliases = ["nowplaying", "np", "playing"], description = "Shows what's currently playing.")
    fun nowPlaying(ctx: Context) {
        val manager = ctx.manager

        val track = manager.player.playingTrack
        //Reset expire time if np has been called.
        manager.scheduler.queue.clearExpireAsync()

        ctx.send {
            setTitle("Now Playing")
            desc { "**[${track.info.embedTitle}](${track.info.embedUri})**" }
            manager.discordFMTrack?.let {
                field("Radio") {
                    val member = ctx.guild?.getMemberById(it.requester)
                    buildString {
                        append("Currently streaming music from radio station `${it.station.capitalize()}`")
                        member?.let {
                            append(", requested by ${member.asMention}")
                        }
                        append('.')
                    }
                }
            }

            field("Requester", true) {
                track.getUserData(TrackContext::class.java)?.requester?.let {
                    ctx.guild?.getMemberById(it)?.asMention
                } ?: "Not Found"
            }

            field("Request Channel", true) {
                track.getUserData(TrackContext::class.java)?.requestedChannel?.let {
                    ctx.guild?.getTextChannelById(it)?.asMention
                } ?: "Not Found"
            }

            addBlankField(true)

            field("Repeating", true) {
                manager.scheduler.repeatOption.name.toLowerCase().capitalize()
            }

            field("Volume", true) {
                "${manager.player.volume}%"
            }

            field("Bass Boost", true) {
                manager.dspFilter.bassBoost.name.toLowerCase().capitalize()
            }

            field("Time", true) {
                if (track.duration == Long.MAX_VALUE) {
                    "`Streaming`"
                } else {
                    val position = Utils.getTimestamp(track.position)
                    val duration = Utils.getTimestamp(track.duration)
                    "`[$position / $duration]`"
                }
            }

            field("Progress", false) {
                val percent = track.position.toDouble() / track.duration
                buildString {
                    for (i in 0 until totalBlocks) {
                        if ((percent * (totalBlocks - 1)).toInt() == i) {
                            append("__**\u25AC**__")
                        } else {
                            append("\u2015")
                        }
                    }
                    append(" **%.1f**%%".format(percent * 100))
                }
            }

            setFooter("Use ${ctx.config.prefix}lyrics current to see the lyrics of the song!")
        }
    }
}