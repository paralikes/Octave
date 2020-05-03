package xyz.gnarbot.gnar.commands.music.dj

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import xyz.gnarbot.gnar.entities.framework.CheckVoiceState
import xyz.gnarbot.gnar.entities.framework.DJ
import xyz.gnarbot.gnar.entities.framework.MusicCog
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.utils.extensions.manager

class Filters : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    private val filters = mapOf(
        "tremolo" to ::modifyTremolo,
        "timescale" to ::modifyTimescale,
        "karaoke" to ::modifyKaraoke
    )

    private val filterString = filters.keys.joinToString("`, `", prefix = "`", postfix = "`")

    @DJ
    @CheckVoiceState
    @Command(aliases = ["filters", "fx", "effects"], description = "Apply audio filters to the music such as speed and pitch")
    fun filter(ctx: Context, filter: String, type: String, value: Double) {
        filters[filter]?.invoke(ctx, type, value, ctx.manager)
                ?: return ctx.send("Invalid filter. Pick one of $filterString, or `status` to view filter status.")
    }

    @SubCommand(description = "Check the current status of filters.")
    fun status(ctx: Context) {
        val manager = ctx.manager
        val karaokeStatus = if (manager.dspFilter.karaokeEnable) "Enabled" else "Disabled"
        val tremoloStatus = if (manager.dspFilter.tremoloEnable) "Enabled" else "Disabled"
        val timescaleStatus = if (manager.dspFilter.timescaleEnable) "Enabled" else "Disabled"

        ctx.send {
            setTitle("Music Effects")
            addField("Karaoke", karaokeStatus, true)
            addField("Timescale", timescaleStatus, true)
            addField("Tremolo", tremoloStatus, true)
        }
    }

    @SubCommand(description = "Clear all filters.")
    fun clear(ctx: Context) {
        ctx.manager.dspFilter.clearFilters()
        ctx.send("Cleared all filters.")
    }

    private fun modifyTimescale(ctx: Context, type: String, amount: Double, manager: MusicManager) {
        val value = amount.coerceIn(0.1, 3.0)

        when (type) {
            "pitch" -> manager.dspFilter.tsPitch = value
            "speed" -> manager.dspFilter.tsSpeed = value
            "rate" -> manager.dspFilter.tsRate = value
            else -> return ctx.send("Invalid choice `${type}`, pick one of `pitch`/`speed`/`rate`.")
        }

        ctx.send("Timescale `${type.toLowerCase()}` set to `$value`")
    }

    private fun modifyTremolo(ctx: Context, type: String, amount: Double, manager: MusicManager) {
        when (type) {
            "depth" -> {
                val depth = amount.coerceIn(0.0, 1.0)
                manager.dspFilter.tDepth = depth.toFloat()
                ctx.send("Tremolo `depth` set to `$depth`")
            }

            "frequency" -> {
                val frequency = amount.coerceAtLeast(0.1)
                manager.dspFilter.tFrequency = frequency.toFloat()
                ctx.send("Tremolo `frequency` set to `$frequency`")
            }
            else -> ctx.send("Invalid choice `${type}`, pick one of `depth`/`frequency`.")
        }
    }

    private fun modifyKaraoke(ctx: Context, type: String, amount: Double, manager: MusicManager) {
        when (type) {
            "level" -> {
                val level = amount.coerceAtLeast(0.0)
                manager.dspFilter.kLevel = level.toFloat()
                return ctx.send("Karaoke `${type}` set to `$level`")
            }
            "band" -> manager.dspFilter.kFilterBand = amount.toFloat()
            "width" -> manager.dspFilter.kFilterWidth = amount.toFloat()
            else -> ctx.send("Invalid choice `${type}`, pick one of `level`/`band`/`width`.")
        }

        ctx.send("Karaoke `${type}` set to `$amount`")
    }
}