package gg.octave.bot.commands.music.search

import com.jagrosh.jdautilities.selector
import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.embedTitle
import gg.octave.bot.commands.music.embedUri
import gg.octave.bot.music.MusicLimitException
import gg.octave.bot.utils.Utils
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.extensions.voiceChannel
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.entities.Cog
import java.awt.Color

class Youtube : Cog {
    @Command(aliases = ["yt"], description = "Search and see YouTube results.")
    fun youtube(ctx: Context, @Greedy query: String) {
        Launcher.players.get(ctx.guild).search("ytsearch:$query", 5) { results ->
            if (results.isEmpty()) {
                return@search ctx.send("No search results for `$query`.")
            }

            val botChannel = ctx.selfMember!!.voiceState?.channel
            val userChannel = ctx.voiceChannel

            if (userChannel == null || botChannel != null && botChannel != userChannel) {
                return@search ctx.send {
                    setColor(Color(141, 20, 0))
                    setAuthor("YouTube Results", "https://www.youtube.com", "https://www.youtube.com/favicon.ico")
                    setThumbnail("https://octave.gg/assets/img/youtube.png")
                    setDescription(
                        results.joinToString("\n") {
                            "**[${it.info.embedTitle}](${it.info.embedUri})**\n" +
                                "**`${Utils.getTimestamp(it.duration)}`** by **${it.info.author}**\n"
                        }
                    )
                    setFooter("Want to play one of these music tracks? Join a voice channel and reenter this command.", null)
                }
            }

            Launcher.eventWaiter.selector {
                setColor(Color(141, 20, 0))
                setTitle("YouTube Results")
                setDescription("Select one of the following options to play them in your current music channel.")
                setUser(ctx.author)

                for (result in results) {
                    addOption("`${Utils.getTimestamp(result.info.length)}` **[${result.info.embedTitle}](${result.info.embedUri})**") {
                        if (ctx.member!!.voiceState!!.inVoiceChannel()) {
                            val manager = try {
                                Launcher.players.get(ctx.guild)
                            } catch (e: MusicLimitException) {
                                return@addOption e.sendToContext(ctx)
                            }

                            if (ctx.data.music.isVotePlay) {
                                Play.startPlayVote(ctx, manager, query.split(" +".toRegex()).toTypedArray(), true, result.info.uri)
                            } else {
                                Play.play(ctx, query.split(" +".toRegex()).toTypedArray(), true, result.info.uri)
                            }
                        } else {
                            ctx.send("You're not in a voice channel anymore!")
                        }
                    }
                }
            }.display(ctx.textChannel!!)
        }
    }
}
