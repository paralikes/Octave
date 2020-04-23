package xyz.gnarbot.gnar.commands.music

import com.jagrosh.jdautilities.paginator
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description
import xyz.gnarbot.gnar.utils.RequestUtil
import xyz.gnarbot.gnar.utils.TextSplitter
import java.net.URLEncoder

@Command(
        aliases = ["lyrics"],
        usage = "(current|search <term>)",
        description = "Look up lyrics of the current playing song or of a song of your choice."
)
@BotInfo(
        id = 193,
        category = Category.MUSIC,
        scope = Scope.VOICE
)

class LyricsCommand : CommandTemplate() {
    @Description("Lyrics of the current song.")
    fun current(context: Context) {
        val manager = context.bot.players.getExisting(context.guild)
            ?: return context.send().info("There's no player to be seen here.").queue()

        val audioTrack = manager.player.playingTrack
            ?: return context.send().info("There's no song playing currently.").queue()

        val title = audioTrack.info.title
        sendLyricsFor(context, title)
    }

    @Description("Searches lyrics.")
    fun search(context: Context, content: String) {
        sendLyricsFor(context, content)
    }

    private fun sendLyricsFor(ctx: Context, title: String) {
        val encodedTitle = URLEncoder.encode(title, Charsets.UTF_8)

        RequestUtil.jsonObject {
            url("https://lyrics.tsu.sh/v1/?q=$encodedTitle")
            header("User-Agent", "Octave (DiscordBot, https://github.com/DankMemer/Octave")
        }.thenAccept {
            if (!it.isNull("error")) {
                return@thenAccept ctx.send().info("No lyrics found for `$title`. Try another song?").queue()
            }

            val lyrics = it.getString("content")
            val pages = TextSplitter.split(lyrics, 1000)

            val songObject = it.getJSONObject("song")
            val fullTitle = songObject.getString("full_title")
            //val icon = songObject.getString("icon")

            ctx.bot.eventWaiter.paginator {
                setUser(ctx.user)
                setEmptyMessage("There should be something here 👀")
                setItemsPerPage(1)
                finally { message -> message!!.delete().queue() }
                title { "Lyrics for $fullTitle" }

                for (page in pages) {
                    entry { page }
                }
            }.display(ctx.textChannel)
        }.exceptionally {
            ctx.send().error(it.localizedMessage).queue()
            return@exceptionally null
        }
    }
}
