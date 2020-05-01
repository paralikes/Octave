package xyz.gnarbot.gnar.commands.music

import com.google.common.collect.Lists
import xyz.gnarbot.gnar.commands.*

@Command(
        aliases = ["sbl", "sblist"],
        description = "Get all of the dank memes"
)
@BotInfo(
        id = 87,
        category = Category.MUSIC
)
class SoundboardListCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        var page = args.firstOrNull()?.toIntOrNull() ?: 1

        context.send().embed("Meme List") {
            val totalEmotes = context.bot.soundManager.map.keys.map { it }

            val pages = Lists.partition(totalEmotes, 10)

            if (page >= pages.size) page = pages.size
            else if (page <= 0) page = 1

            val emotePage = pages[page - 1]

            desc {
                buildString {
                    emotePage.forEach {
                        append("$it\n")
                    }
                }
            }

            footer { "Page [$page/${pages.size}], Sounds from https://www.randommemes.website/" }
        }.action().queue()
    }
}