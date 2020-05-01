package xyz.gnarbot.gnar.commands.music.search

import com.jagrosh.jdautilities.selector
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.embedTitle
import xyz.gnarbot.gnar.music.MusicLimitException
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.utils.Utils
import java.awt.Color

@Command(
        aliases = ["youtube", "yt"],
        usage = "(query...)",
        description = "Search and see YouTube results."
)
@BotInfo(
        id = 64,
        scope = Scope.TEXT,
        category = Category.MUSIC
)
class YoutubeCommand : xyz.gnarbot.gnar.commands.CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        if (!context.bot.configuration.searchEnabled) {
            context.send().issue("Search is currently disabled. Try direct links instead.").queue()
            return
        }

        if (args.isEmpty()) {
            context.send().issue("Input a query to search YouTube.").queue()
            return
        }

        val query = args.joinToString(" ")

        context.bot.players.get(context.guild).search("ytsearch:$query", 5) { results ->
            if (results.isEmpty()) {
                context.send().issue("No search results for `$query`.").queue()
                return@search
            }

            val botChannel = context.selfMember.voiceState?.channel
            val userChannel = context.guild.getMember(context.message.author)?.voiceState?.channel

            if (!context.bot.configuration.musicEnabled || userChannel == null || botChannel != null && botChannel != userChannel) {
                context.send().embed {
                    setAuthor("YouTube Results", "https://www.youtube.com", "https://www.youtube.com/favicon.ico")
                    thumbnail { "https://octave.gg/assets/img/youtube.png" }
                    color { Color(141, 20, 0) }

                    desc {
                        buildString {
                            for (result in results) {

                                val title = result.info.embedTitle
                                val url = result.info.uri
                                val length = Utils.getTimestamp(result.duration)
                                val author = result.info.author

                                append("**[$title]($url)**\n")
                                append("**`").append(length).append("`** by **").append(author).append("**\n")
                            }
                        }
                    }

                    setFooter("Want to play one of these music tracks? Join a voice channel and reenter this command.", null)
                }.action().queue()
                return@search
            } else {
                context.bot.eventWaiter.selector {
                    title { "YouTube Results" }
                    desc { "Select one of the following options to play them in your current music channel." }
                    color { Color(141, 20, 0) }

                    setUser(context.user)

                    for (result in results) {
                        addOption("`${Utils.getTimestamp(result.info.length)}` **[${result.info.embedTitle}](${result.info.uri})**") {
                            if (context.member.voiceState!!.inVoiceChannel()) {
                                val manager = try {
                                    context.bot.players.get(context.guild)
                                } catch (e: MusicLimitException) {
                                    e.sendToContext(context)
                                    return@addOption
                                }

                                if(context.data.music.isVotePlay) {
                                    PlayCommand.startPlayVote(context, manager, args, true, result.info.uri)
                                } else {
                                    PlayCommand.play(context, args, true, result.info.uri)
                                }
                            } else {
                                context.send().issue("You're not in a voice channel anymore!").queue()
                            }
                        }
                    }
                }.display(context.textChannel)
            }
        }
    }
}



