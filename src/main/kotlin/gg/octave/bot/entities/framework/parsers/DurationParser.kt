package gg.octave.bot.entities.framework.parsers

import gg.octave.bot.utils.toDuration
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.parsers.Parser
import java.time.Duration
import java.util.*

class DurationParser : Parser<Duration> {
    override fun parse(ctx: Context, param: String): Optional<Duration> {
        return try {
            Optional.of(param.toDuration())
        } catch (e: RuntimeException) {
            Optional.empty()
        }
    }
}
