package gg.octave.bot.entities.framework

import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.parsers.Parser
import java.util.*

open class EnumParser<T : Enum<*>>(private val enumClass: Class<T>) : Parser<T> {
    override fun parse(ctx: Context, param: String): Optional<T> {
        val upper = param.toUpperCase()
        return Optional.ofNullable(enumClass.enumConstants.firstOrNull { it.name == upper })
    }
}
