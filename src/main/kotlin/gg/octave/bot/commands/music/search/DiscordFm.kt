package gg.octave.bot.commands.music.search

import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.PLAY_MESSAGE
import gg.octave.bot.music.DiscordFMTrackContext
import gg.octave.bot.music.MusicLimitException
import gg.octave.bot.utils.DiscordFM
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import org.apache.commons.lang3.StringUtils

class DiscordFm : Cog {
    @Command(aliases = ["dfm"], description = "Stream random songs from some radio stations.")
    fun radio(ctx: Context, station: String?) {
        if (station == null) {
            val stations = DiscordFM.LIBRARIES.joinToString("\n") { "• `${it.capitalize()}`" }

            return ctx.send {
                setDescription(
                    buildString {
                        appendln("Stream random songs from radio stations!")
                        appendln("Select and stream a station using `${ctx.trigger}radio <station name>`.")
                        append("Stop streaming songs from a station with `${ctx.trigger}radio stop`,")
                    }
                )
                addField("Available Stations", stations, false)
            }
        }

        val query = station.toLowerCase()
        // quick check for incomplete query
        // classic -> classical
        val library = DiscordFM.LIBRARIES.firstOrNull { it.contains(query) }
            ?: DiscordFM.LIBRARIES.minBy { StringUtils.getLevenshteinDistance(it, query) }
            ?: return ctx.send("Library $query doesn't exist. Available stations: `${DiscordFM.LIBRARIES.contentToString()}`.")

        val manager = try {
            Launcher.players.get(ctx.guild)
        } catch (e: MusicLimitException) {
            return e.sendToContext(ctx)
        }

        val track = Launcher.discordFm.getRandomSong(library)
            ?: return ctx.send("Couldn't find any tracks in that library.")

        DiscordFMTrackContext(library, ctx.author.idLong, ctx.textChannel!!.idLong).let {
            manager.discordFMTrack = it
            manager.loadAndPlay(ctx, track, it, "Now streaming random tracks from the `$library` radio station!")
        }
    }

    @SubCommand
    fun stop(ctx: Context) {
        val manager = Launcher.players.getExisting(ctx.guild)
            ?: return ctx.send("There's no music player in this guild.\n$PLAY_MESSAGE")

        if (manager.discordFMTrack == null) {
            return ctx.send("I'm not streaming random songs from a radio station.")
        }

        val station = manager.discordFMTrack!!.station.capitalize()
        manager.discordFMTrack = null

        ctx.send {
            setTitle("Radio")
            setDescription("No longer streaming random songs from the `$station` station.")
        }
    }
}
