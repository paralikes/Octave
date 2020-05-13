package gg.octave.bot.listeners

import gg.octave.bot.Launcher
import gg.octave.bot.db.guilds.GuildData
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.entities.framework.Usage
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.getDisplayValue
import gg.octave.bot.utils.hasAnyRoleId
import gg.octave.bot.utils.hasAnyRoleNamed
import io.sentry.Sentry
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.SubCommandFunction
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.DefaultCommandEventAdapter
import me.devoxin.flight.internal.arguments.Argument
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.VoiceChannel
import java.time.Duration
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class FlightEventAdapter : DefaultCommandEventAdapter() {
    fun generateDefaultUsage(arguments: List<Argument>): String {
        return buildString {
            for (arg in arguments) {
                val value = when (arg.type) {
                    String::class.java -> "\"some text\""
                    Int::class, java.lang.Integer::class.java, Long::class.java, java.lang.Long::class.java -> "0"
                    Double::class.java, java.lang.Double::class.java -> "0.0"
                    Member::class.java, User::class.java -> "@User"
                    TextChannel::class.java -> "#general"
                    VoiceChannel::class.java -> "Music"
                    Boolean::class.java, java.lang.Boolean::class.java -> "yes"
                    Duration::class.java -> "20m"
                    else -> {
                        if (arg.type.isEnum) {
                            arg.type.enumConstants.first().toString().toLowerCase()
                        } else {
                            "[Unknown Type, report to devs]"
                        }
                    }
                }
                append(value)
                append(" ")
            }
        }.trim()
    }

    override fun onBadArgument(ctx: Context, command: CommandFunction, error: BadArgument) {
        if (error.argument.type.isEnum) {
            val options = error.argument.type.enumConstants.map { it.toString().toLowerCase() }
            return ctx.send {
                setTitle("Help | ${command.name}")
                setDescription("You specified an invalid argument for `${error.argument.name}`.")
                addField("Valid Options", options.joinToString("`\n- `", prefix = "- `", postfix = "`"), true)
            }
        }

        val executed = ctx.invokedCommand
        val arguments = executed.arguments
        val commandLayout = buildString {
            append(ctx.trigger)
            append(command.name)

            if (executed is SubCommandFunction) {
                append(" ")
                append(executed.name)
            }
        }

        val syntax = buildString {
            append(commandLayout)
            append(" ")
            for (argument in arguments) {
                append(argument.name)
                append(" ")
            }
        }.trim()

        val usage = executed.method.findAnnotation<Usage>()?.description
            ?: generateDefaultUsage(arguments)

        ctx.send {
            setTitle("Help | ${command.name}")
            setDescription("You specified an invalid argument for `${error.argument.name}`")
            addField("Syntax", "`$syntax`", false)
            addField("Example Usage", "`$commandLayout $usage`", false)
            addField("Still Confused?", "Head over to our [#support channel](https://discord.gg/musicbot)", false)
        }
    }

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        error.printStackTrace()
        Sentry.capture(error)
        ctx.send("An error was encountered while parsing the arguments for this command.\n" +
            "The error has been logged. We apologise for any inconvenience caused!")
    }

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        error.printStackTrace()
        Sentry.capture(error)
        ctx.send("The command encountered an error, which has been logged.\n" +
            "We apologise for any inconvenience caused!")
    }

    override fun onCommandCooldown(ctx: Context, command: CommandFunction, cooldown: Long) {
        ctx.send("This command is on cool-down. Wait ${getDisplayValue(cooldown, true)}.")
    }

    @ExperimentalStdlibApi
    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        if (ctx.guild == null) {
            return false
        }

        if (ctx.member!!.hasPermission(Permission.ADMINISTRATOR)
                || ctx.member!!.hasPermission(Permission.MANAGE_SERVER)
                || ctx.author.idLong in ctx.config.admins) {
            return true
        }

        val data = ctx.data

        //Don't send a message if it's just ignored.
        if (isIgnored(ctx, data, ctx.member!!)) {
            return false
        }

        if (command.category == "Music" || command.category == "Dj" || command.category == "Search") {
            if (ctx.member!!.voiceState?.channel == null) {
                ctx.send("You're not in a voice channel.")
                return false
            }

            if (ctx.member!!.voiceState?.channel == ctx.guild!!.afkChannel) {
                ctx.send("You can't play music in the AFK channel.")
                return false
            }

            if (data.music.channels.isNotEmpty() && ctx.member!!.voiceState?.channel?.id !in data.music.channels) {
                val channels = data.music.channels
                    .mapNotNull { ctx.guild!!.getVoiceChannelById(it)?.name }
                    .joinToString(", ")

                ctx.send("Music can only be played in: `$channels`, since this server has set it/them as a designated voice channel.")
                return false
            }
        }

        if (command.method.hasAnnotation<DJ>() || data.command.isDjOnlyMode) {
            return data.music.isDisableDj || isDJ(ctx)
        }

        return true
    }

    private fun isIgnored(ctx: Context, data: GuildData, member: Member): Boolean {
        return member.user.id in data.ignored.users || ctx.textChannel!!.id in data.ignored.channels
                || data.ignored.roles.any { id -> member.roles.any { it.id == id } }
    }


    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        Launcher.datadog.incrementCounter("bot.commands_ran")
    }

    override fun onBotMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {
        val formatted = permissions.joinToString("`\n`", prefix = "`", postfix = "`", transform=Permission::getName)

        if (Permission.MESSAGE_EMBED_LINKS in permissions) {
            return ctx.send("__Missing Permissions__\n\nThis command requires the following permissions:\n$formatted")
        }
        // Perhaps the above should be in `preInvoke` with a message when perm is missing?
        // I'm pretty sure we don't label embed_links as a requirement for all commands anyway.

        ctx.send {
            setTitle("Missing Permissions")
            setDescription("I need the following permissions:\n$formatted")
        }
    }

    override fun onUserMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) {
        val formatted = permissions.joinToString("`\n`", prefix = "`", postfix = "`", transform=Permission::getName)

        ctx.send {
            setTitle("Missing Permissions")
            setDescription("You need the following permissions:\n$formatted")
        }
    }

    companion object {
        fun isDJ(ctx: Context, send: Boolean = true): Boolean {
            val data = ctx.data
            val memberSize = ctx.selfMember!!.voiceState?.channel?.members?.size
            val djRole = data.command.djRole

            val djRolePresent = if (djRole != null) ctx.member!!.hasAnyRoleId(djRole) || data.music.djRoles.any { ctx.member!!.hasAnyRoleId(it) } else false
            val memberAmount = if (memberSize != null) memberSize <= 2 else false
            val admin = ctx.member!!.hasPermission(Permission.MANAGE_SERVER)

            if (ctx.member!!.hasAnyRoleNamed("DJ") || djRolePresent || memberAmount || admin) {
                return true
            }

            val extra = when (djRolePresent) {
                true -> ", or a role called ${djRole?.let { ctx.guild!!.getRoleById(it)?.name }}"
                false -> ""
            }

            if (send) {
                ctx.send("You need a role called DJ$extra.\nThis can be bypassed if you're an admin (either Manage Server or Administrator) or you're alone with the bot.")
            }
            return false
        }
    }
}
