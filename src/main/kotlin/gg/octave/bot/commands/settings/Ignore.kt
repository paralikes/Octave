package gg.octave.bot.commands.settings

import gg.octave.bot.utils.extensions.DEFAULT_SUBCOMMAND
import gg.octave.bot.utils.extensions.data
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel

class Ignore : Cog {
    @Command(description = "Configure user/channel/role ignoring.", userPermissions = [Permission.MANAGE_SERVER])
    fun ignore(ctx: Context) = DEFAULT_SUBCOMMAND(ctx)

    @SubCommand(description = "Toggle ignore for a user.")
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

    @SubCommand(description = "Toggle ignore for a channel.")
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

    @SubCommand(description = "Toggle ignore for a role.")
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

    fun <T : IMentionable> mapper(type: String, data: Set<String>, transform: (String) -> T?): String {
        return data.mapNotNull(transform)
            .takeIf { it.isNotEmpty() }
            ?.map(IMentionable::getAsMention)
            ?.joinToString("\n") { "• $it" }
            ?: "No $type are ignored."
    }

    fun mapString(type: String, data: Set<String>, transform: (String) -> String): String {
        return data.map(transform)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n") { "• $it" }
            ?: "No $type are ignored."
    }

    @SubCommand(description = "Lists all entities that are currently being ignored.")
    fun list(ctx: Context) {
        val ignored = ctx.data.ignored

        ctx.send {
            setColor(0x9570D3)
            setTitle("Ignored Entities")
            addField("Users", mapString("users", ignored.users) { "<@$it>" }, true)
            addField("Channel", mapper("channels", ignored.channels, ctx.guild!!::getTextChannelById), true)
            addField("Roles", mapper("roles", ignored.roles, ctx.guild!!::getRoleById), true)
        }
    }
}
