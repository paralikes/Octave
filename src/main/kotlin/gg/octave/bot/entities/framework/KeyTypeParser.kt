package gg.octave.bot.entities.framework

import gg.octave.bot.db.PremiumKey
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.parsers.Parser
import java.util.*

class KeyTypeParser : Parser<PremiumKey.Type> {
    override fun parse(ctx: Context, param: String): Optional<PremiumKey.Type> {
        return try {
            Optional.of(PremiumKey.Type.valueOf(param))
        } catch (e: IllegalArgumentException) {
            Optional.empty()
        }
    }
}
