package gg.octave.bot.entities.framework.parsers

import gg.octave.bot.db.PremiumKey

class KeyTypeParser : EnumParser<PremiumKey.Type>(PremiumKey.Type::class.java)
