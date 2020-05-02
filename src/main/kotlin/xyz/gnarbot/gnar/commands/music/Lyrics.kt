package xyz.gnarbot.gnar.commands.music

import com.jagrosh.jdautilities.paginator
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.utils.TextSplitter
import xyz.gnarbot.gnar.utils.RequestUtil
import xyz.gnarbot.gnar.utils.extensions.bot
import java.net.URLEncoder

class Lyrics : Cog {
    @Command(description = "Shows the lyrics of the current song")
    fun lyrics(ctx: Context) {
        val manager = ctx.bot.players.getExisting(ctx.guild)
                ?: return ctx.send("There's no player to be seen here.")

        val audioTrack = manager.player.playingTrack
                ?: return ctx.send("There's no song playing currently.")

        val title = audioTrack.info.title
        sendLyricsFor(ctx, title)
    }

    @SubCommand(description = "Search for specific song lyrics")
    fun search(ctx: Context, content: String?) {
        sendLyricsFor(ctx, content)
    }

    private fun sendLyricsFor(ctx: Context, title: String?) {
        val encodedTitle = URLEncoder.encode(title, Charsets.UTF_8)

        RequestUtil.jsonObject {
            url("https://lyrics.tsu.sh/v1/?q=$encodedTitle")
            header("User-Agent", "Octave (DiscordBot, https://github.com/DankMemer/Octave")
        }.thenAccept { it ->
            if (!it.isNull("error")) {
                return@thenAccept ctx.send("No lyrics found for `$title`. Try another song?")
            }

            val lyrics = it.getString("content")
            val pages = TextSplitter.split(lyrics, 1000)

            val songObject = it.getJSONObject("song")
            val fullTitle = songObject.getString("full_title")

            ctx.textChannel?.let { tx ->
                ctx.bot.eventWaiter.paginator {
                    setUser(ctx.author)
                    setEmptyMessage("There should be something here 👀")
                    setItemsPerPage(1)
                    finally { message -> message!!.delete().queue() }
                    title { "Lyrics for $fullTitle" }

                    for (page in pages) {
                        entry { page }
                    }
                }.display(tx)
            }
        }.exceptionally {
            ctx.send(it.localizedMessage)
            return@exceptionally null
        }
    }
}