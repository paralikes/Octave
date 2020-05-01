package xyz.gnarbot.gnar.commands.dispatcher.predicates

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.Scope
import xyz.gnarbot.gnar.utils.hasAnyRoleId
import xyz.gnarbot.gnar.utils.hasAnyRoleNamed
import java.util.function.BiPredicate

class PermissionPredicate : BiPredicate<CommandExecutor, Context> {
    override fun test(cmd: CommandExecutor, context: Context): Boolean {
        if (context.member.hasPermission(Permission.ADMINISTRATOR) || context.member.hasPermission(Permission.MANAGE_SERVER)) {
            return true
        }

        if ((cmd.botInfo.permissions.isEmpty() && !cmd.botInfo.djLock && cmd.botInfo.roleRequirement.isEmpty())) {
            return true
        }

        if(cmd.botInfo.djLock || context.data.command.isDjOnlyMode) {
            //Don't return here if it's false because it's handled down there. God knows why lol
            if(isDJ(context) || context.data.music.isDisableDj)
                return true
        }

        if (context.member.hasAnyRoleNamed(cmd.botInfo.roleRequirement)
                && cmd.botInfo.scope.checkPermission(context, *cmd.botInfo.permissions)) {
            return true
        }

        context.send().error(buildString {
            append("This command requires ")

            val permissionNotEmpty = cmd.botInfo.permissions.isNotEmpty()

            if(cmd.botInfo.djLock && context.data.command.djRole != null) {
                append("a role named ")
                append(context.guild.getRoleById(context.data.command.djRole!!)?.name)
            } else if (cmd.botInfo.djLock && context.data.command.djRole == null) {
                append("a role named DJ")
            }

            if (cmd.botInfo.roleRequirement.isNotEmpty()) {
                append("a role named")
                append(cmd.botInfo.roleRequirement)

                if (permissionNotEmpty) {
                    append(" and ")
                }
            }

            if (permissionNotEmpty) {
                append("the permissions `")
                append(cmd.botInfo.permissions.map(Permission::getName))
                append("` in ")

                when (cmd.botInfo.scope) {
                    Scope.GUILD -> {
                        append("the guild `")
                        append(context.guild.name)
                    }
                    Scope.TEXT -> {
                        append("the text channel `")
                        append(context.textChannel.name)
                    }
                    Scope.VOICE -> {
                        append("the voice channel `")
                        append(context.voiceChannel.name)
                    }
                }
            }

            append(".")
        }).queue()
        return false
    }

    companion object {
        fun isDJ(context: Context): Boolean {
            val memberSize = context.selfMember.voiceState?.channel?.members?.size
            val djRole = context.data.command.djRole

            val djRolePresent = if(djRole != null) context.member.hasAnyRoleId(djRole) else false
            val memberAmount = if(memberSize != null) memberSize <= 2 else false
            val admin = context.member.permissions.contains(Permission.MANAGE_SERVER) || context.member.permissions.contains(Permission.ADMINISTRATOR)

            if(context.member.hasAnyRoleNamed("DJ") || djRolePresent || memberAmount || admin) {
                return true
            }

            return false;
        }
    }
}