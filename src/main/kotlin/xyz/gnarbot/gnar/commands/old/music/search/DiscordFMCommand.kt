package xyz.gnarbot.gnar.commands.music.search

import org.apache.commons.lang3.StringUtils
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE
import xyz.gnarbot.gnar.music.DiscordFMTrackContext
import xyz.gnarbot.gnar.music.MusicLimitException
import xyz.gnarbot.gnar.utils.DiscordFM

@Command(
        aliases = ["radio", "dfm"],
        usage = "(station name)|stop",
        description = "Stream random songs from some radio stations."
)
@BotInfo(
        id = 82,
        scope = Scope.VOICE,
        category = Category.MUSIC
)
class DiscordFMCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        if (args.isEmpty()) {
            context.send().embed("Radio") {
                desc {
                    buildString {
                        append("Stream random songs from radio stations!\n")
                        append("Select and stream a station using `${config.prefix}radio (station name)`.\n")
                        append("Stop streaming songs from a station with `${config.prefix}radio stop`,")
                    }
                }

                field("Available Stations") {
                    buildString {
                        DiscordFM.LIBRARIES.forEach {
                            append("• `").append(it.capitalize()).append("`\n")
                        }
                    }
                }
            }.action().queue()
            return
        }

        if (args[0] == "stop") {
            val manager = context.bot.players.getExisting(context.guild)

            if (manager == null) {
                context.send().error("There's no music player in this guild.\n$PLAY_MESSAGE").queue()
                return
            }

            if (manager.discordFMTrack == null) {
                context.send().error("I'm not streaming random songs from a radio station.").queue()
                return
            }

            val station = manager.discordFMTrack!!.station.capitalize()
            manager.discordFMTrack = null

            context.send().embed("Radio") {
                desc { "No longer streaming random songs from the `$station` station." }
            }.action().queue()
            return
        }

        val query = args.joinToString(" ").toLowerCase()

        // quick check for incomplete query
        // classic -> classical
        var library = DiscordFM.LIBRARIES.firstOrNull { it.contains(query) }
        if (library == null) {
            library = DiscordFM.LIBRARIES.minBy { StringUtils.getLevenshteinDistance(it, query) }
        }

        if (library == null) {
            context.send().error("Library $query doesn't exist. Available stations: `${DiscordFM.LIBRARIES!!.contentToString()}`.").queue()
            return
        }

        val manager = try {
            context.bot.players.get(context.guild)
        } catch (e: MusicLimitException) {
            e.sendToContext(context)
            return
        }

        DiscordFMTrackContext(context.bot, library, context.user.idLong, context.textChannel.idLong).let {
            manager.discordFMTrack = it
            manager.loadAndPlay(context,
                    context.bot.discordFM.getRandomSong(library),
                    it,
                    "Now streaming random tracks from the `$library` radio station!"
            )
        }
    }
}