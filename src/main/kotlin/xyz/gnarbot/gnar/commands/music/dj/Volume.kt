package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.manager

class Volume : MusicCog(false, false, true) {
    private val totalBlocks = 20

    @DJ
    @CheckVoiceState
    @Command(aliases = ["v"], description = "Set the volume of the music player.")
    fun volume(ctx: Context, amount: String?) {
        if(amount.isNullOrEmpty()) {
            val volume = ctx.manager.player.volume.toDouble()
            val max = volume.coerceIn(0.0, 100.0)

            val percent = (volume / max).coerceIn(0.0, 1.0)
            val message = buildString {
                for (i in 0 until totalBlocks) {
                    if ((percent * (totalBlocks - 1)).toInt() == i) {
                        append("__**\u25AC**__")
                    } else {
                        append("\u2015")
                    }
                }
                append(" **%.0f**%%".format(percent * max))
            }


            return ctx.send {
                setTitle("Volume")
                setDescription(message)
                setFooter("Set the volume by using ${ctx.config.prefix}volume (int)", null)
            }
        }

        val increment = amount.toInt().coerceIn(0, 150)

        val old = ctx.manager.player.volume
        ctx.manager.player.volume = increment

        val max = increment.coerceIn(0, 100)
        val percent = (increment.toDouble() / max).coerceIn(0.0, 1.0)
        val message = buildString {
            for (i in 0 until totalBlocks) {
                if ((percent * (totalBlocks - 1)).toInt() == i) {
                    append("__**\u25AC**__")
                } else {
                    append("\u2015")
                }
            }
            append(" **%.0f**%%".format(percent * max))
        }

        ctx.send {
            setTitle("Volume")
            setDescription(message)
            setFooter("Volume changed from $old% to ${ctx.manager.player.volume}%")
        }
    }
}