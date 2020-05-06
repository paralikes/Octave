package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Volume : MusicCog {
    override fun requirePlayer() = true

    private val totalBlocks = 20
    private val maximumVolume = 150

    @DJ
    @CheckVoiceState
    @Command(aliases = ["v", "vol"], description = "Set the volume of the music player.")
    fun volume(ctx: Context, amount: Int?) {
        if (amount == null) {
            val volume = ctx.manager.player.volume.coerceIn(0, maximumVolume)
            val bar = buildBar(volume, maximumVolume)

            return ctx.send {
                setTitle("Volume")
                setDescription(bar)
                setFooter("Set the volume by using ${ctx.config.prefix}volume (number)", null)
            }
        }

        val newVolume = amount.coerceIn(0, maximumVolume)
        val old = ctx.manager.player.volume
        val bar = buildBar(newVolume, maximumVolume)

        ctx.manager.player.volume = newVolume

        ctx.send {
            setTitle("Volume")
            setDescription(bar)
            setFooter("Volume changed from $old% to ${ctx.manager.player.volume}%")
        }
    }

    private fun buildBar(value: Int, maximum: Int): String {
        val percent = (value.toDouble() / maximum).coerceIn(0.0, 1.0)

        return buildString {
            for (i in 0 until totalBlocks) {
                if ((percent * (totalBlocks - 1)).toInt() == i) {
                    append("__**\u25AC**__")
                } else {
                    append("\u2015")
                }
            }
            append(" **%.0f**%%".format(percent * maximum))
        }
    }
}
