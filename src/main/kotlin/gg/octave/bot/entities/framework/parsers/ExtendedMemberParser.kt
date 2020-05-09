package gg.octave.bot.entities.framework.parsers

import me.devoxin.flight.api.Context
import me.devoxin.flight.internal.parsers.MemberParser
import me.devoxin.flight.internal.parsers.Parser
import net.dv8tion.jda.api.entities.Member
import java.util.*

class ExtendedMemberParser : Parser<Member> {
    override fun parse(ctx: Context, param: String): Optional<Member> {
        val parsed = defaultMemberParser.parse(ctx, param)

        return parsed.or {
            val mentioned = ctx.message.mentionedMembers.firstOrNull { it.asMention == param }
            Optional.ofNullable(mentioned)
        }
    }

    companion object {
        private val defaultMemberParser = MemberParser()
    }
}
