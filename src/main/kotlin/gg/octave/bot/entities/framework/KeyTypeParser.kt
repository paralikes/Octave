package gg.octave.bot.entities.framework

import gg.octave.bot.db.PremiumKey
import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.parsers.Parser
import java.util.*

class KeyTypeParser : EnumParser<PremiumKey.Type>(PremiumKey.Type::class.java)
