package gg.octave.bot.utils.extensions

import me.devoxin.flight.api.CommandClientBuilder
import me.devoxin.flight.api.entities.Invite
import me.devoxin.flight.internal.parsers.*

fun CommandClientBuilder.registerAlmostAllParsers(): CommandClientBuilder {
    val booleanParser = BooleanParser()
    addCustomParser(Boolean::class.java, booleanParser)
    addCustomParser(java.lang.Boolean::class.java, booleanParser)

    val doubleParser = DoubleParser()
    addCustomParser(Double::class.java, doubleParser)
    addCustomParser(java.lang.Double::class.java, doubleParser)

    val floatParser = FloatParser()
    addCustomParser(Float::class.java, floatParser)
    addCustomParser(java.lang.Float::class.java, floatParser)

    val intParser = IntParser()
    addCustomParser(Int::class.java, intParser)
    addCustomParser(java.lang.Integer::class.java, intParser)

    val longParser = LongParser()
    addCustomParser(Long::class.java, longParser)
    addCustomParser(java.lang.Long::class.java, longParser)

    // JDA entities
    val inviteParser = InviteParser()
    addCustomParser(Invite::class.java, inviteParser)
    addCustomParser(net.dv8tion.jda.api.entities.Invite::class.java, inviteParser)

    //addCustomParser(MemberParser())
    addCustomParser(UserParser())
    addCustomParser(RoleParser())
    addCustomParser(TextChannelParser())
    addCustomParser(VoiceChannelParser())

    // Custom entities
    addCustomParser(EmojiParser())
    addCustomParser(StringParser())
    addCustomParser(SnowflakeParser())
    addCustomParser(UrlParser())

    return this
}
