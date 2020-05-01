package xyz.gnarbot.gnar.commands.settings

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import me.devoxin.flight.internal.parsers.MemberParser
import me.devoxin.flight.internal.parsers.RoleParser
import me.devoxin.flight.internal.parsers.TextChannelParser
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import xyz.gnarbot.gnar.utils.extensions.DEFAULT_SUBCOMMAND
import xyz.gnarbot.gnar.utils.extensions.data

class Ignore : Cog {
    @Command
    fun ignore(ctx: Context) = DEFAULT_SUBCOMMAND(ctx)

    @SubCommand
    fun user(ctx: Context, @Greedy member: Member) {
        val data = ctx.data
        val ignored = data.ignored

        if (member.id in ignored.users) {
            ignored.users.remove(member.id)
            ctx.send("No longer ignoring user ${member.asMention}.")
        } else {
            ignored.users.add(member.id)
            ctx.send("Now ignoring user ${member.asMention}.")
        }

        data.save()
    }

    @SubCommand
    fun channel(ctx: Context, @Greedy channel: TextChannel) {
        val data = ctx.data
        val ignored = data.ignored

        if (channel.id in ignored.channels) {
            ignored.channels.remove(channel.id)
            ctx.send("No longer ignoring channel ${channel.asMention}.")
        } else {
            ignored.channels.add(channel.id)
            ctx.send("Now ignoring channel ${channel.asMention}.")
        }

        data.save()
    }

    @SubCommand
    fun role(ctx: Context, @Greedy role: Role) {
        val data = ctx.data
        val ignored = data.ignored

        if (role.id in ignored.roles) {
            ignored.roles.remove(role.id)
            ctx.send("No longer ignoring role ${role.asMention}.")
        } else {
            ignored.roles.add(role.id)
            ctx.send("Now ignoring role ${role.asMention}.")
        }

        data.save()
    }

    fun <T: IMentionable> mapper(type: String, data: Set<String>, transform: (String) -> T?): String {
        if (data.isEmpty()) {
            return "No $type are ignored."
        }

        return data.mapNotNull(transform)
            .map(IMentionable::getAsMention)
            .joinToString("\n") { "• $it" }
    }

    @SubCommand
    fun list(ctx: Context) {
        val ignored = ctx.data.ignored

        ctx.send {
            setColor(0x9570D3)
            setTitle("Ignored Entities")
            addField("Users", mapper("users", ignored.users, ctx.guild!!::getMemberById), true)
            addField("Channel", mapper("channels", ignored.channels, ctx.guild!!::getTextChannelById), true)
            addField("Roles", mapper("roles", ignored.roles, ctx.guild!!::getRoleById), true)
        }
    }
}
