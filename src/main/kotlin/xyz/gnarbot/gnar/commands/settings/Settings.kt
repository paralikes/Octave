package xyz.gnarbot.gnar.commands.settings

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.utils.extensions.DEFAULT_SUBCOMMAND
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.premiumGuild
import xyz.gnarbot.gnar.utils.toDuration
import java.lang.RuntimeException

class Settings : Cog {
    @Command(aliases = ["setting", "set", "config", "configuration", "configure", "opts", "options"],
        description = "Change music settings.", guildOnly = true, userPermissions = [Permission.MANAGE_SERVER])
    fun settings(ctx: Context) = DEFAULT_SUBCOMMAND(ctx)

    @SubCommand(description = "Resets the settings for the guild.")
    fun reset(ctx: Context) {
        ctx.data.apply { reset(); save() }
        ctx.send {
            setColor(0x9570D3)
            setTitle("Settings")
            setDescription("The settings for this server have been reset.")
        }
    }

    @SubCommand(aliases = ["autodel"], description = "Toggle whether the bot auto-deletes its responses.")
    fun autodelete(ctx: Context, toggle: Boolean) {
        ctx.data.let {
            it.command.isAutoDelete = toggle
            it.save()
        }

        val send = if (!toggle) "The bot will no longer automatically delete messages after 10 seconds."
        else "The bot will now delete messages after 10 seconds."

        ctx.send(send)
    }

    @SubCommand(aliases = ["ta"], description = "Set the channel used for track announcements.")
    fun announcements(ctx: Context, toggle: Boolean) {
        ctx.data.let {
            it.music.announce = toggle
            it.save()
        }

        val send = if (toggle) "Announcements for music enabled." else "Announcements for music disabled."
        ctx.send(send)
    }

    @SubCommand(description = "Toggles whether only DJs can use the bot.")
    fun djonly(ctx: Context, toggle: Boolean) {
        ctx.data.let {
            it.command.isDjOnlyMode = toggle
            it.save()
        }

        val send = if (toggle) "Enabled DJ-only mode." else "Disabled DJ-only mode."
        ctx.send(send)
    }

    @SubCommand(aliases = ["djrequirement"], description = "Set whether DJ-only commands can be used by all.")
    fun requiredj(ctx: Context, toggle: Boolean) {
        ctx.data.let {
            it.music.isDisableDj = toggle
            it.save()
        }

        val send = if (toggle) "DJ commands now require the DJ role." else "DJ commands can be now run by everyone."
        ctx.send(send)
    }

    @SubCommand(aliases = ["votequeue", "vp", "vq"], description = "Toggle whether voting is enabled for track queueing.")
    fun voteplay(ctx: Context, toggle: Boolean) {
        ctx.data.let {
            it.music.isVotePlay = toggle
            it.save()
        }

        val send = if (toggle) "Enabled vote-play." else "Disabled vote-play."
        ctx.send(send)
    }

    @SubCommand(aliases = ["vc"], description = "Toggles a voice-channel as a dedicated music channel.")
    fun voicechannel(ctx: Context, channel: VoiceChannel) {
        val data = ctx.data

        if (channel.id in data.music.channels) {
            data.music.channels.remove(channel.id)
            data.save()
            return ctx.send("${channel.name} is no longer a designated music channel.")
        }

        if (channel == ctx.guild!!.afkChannel) {
            return ctx.send("`${channel.name}` is the AFK channel, you can't play music there.")
        }

        data.music.channels.add(channel.id)
        data.save()
        ctx.send("`${channel.name}` is now a designated music channel.")
    }

    @SubCommand(aliases = ["sl"], description = "Set the maximum song length. \"reset\" to reset.")
    fun songlength(ctx: Context, content: String) {
        val data = ctx.data

        if (content == "reset") {
            data.music.maxSongLength = 0
            data.save()
            return ctx.send("Song length limit reset.")
        }

        val duration = try {
            content.toDuration()
        } catch (e: RuntimeException) {
            return ctx.send("Wrong duration specified: Expected something like `40 minutes`")
        }

        val config = Bot.getInstance().configuration
        val premiumGuild = ctx.premiumGuild
        val durationLimit = premiumGuild?.songLengthQuota ?: config.durationLimit.toMillis()

        if (duration.toMillis() > durationLimit) {
            return ctx.send("This is too much. The limit is ${config.durationLimitText}.")
        }

        if (duration.toMinutes() < 1) {
            return ctx.send("That's too little. It has to be more than 1 minute.")
        }

        data.music.maxSongLength = duration.toMillis()
        data.save()
        ctx.send("Successfully set song length limit to $content.")
    }

    @SubCommand(aliases = ["ac"], description = "Set the music announcement channel. Omit to reset.")
    fun announcementchannel(ctx: Context, textChannel: TextChannel?) {
        ctx.data.let {
            it.music.announcementChannel = textChannel?.id
            it.save()
        }

        val out = textChannel?.let { "Successfully set music announcement channel to ${it.asMention}" }
            ?: "Successfully reset the music announcement channel."

        ctx.send(out)
    }

    @SubCommand(aliases = ["djr", "dr"], description = "Sets the DJ role. Omit to reset.")
    fun djrole(ctx: Context, role: Role?) {
        ctx.data.let {
            it.command.djRole = role?.id
            it.save()
        }

        val out = role?.let { "Successfully set the DJ role to ${it.asMention}" }
            ?: "Successfully reset the DJ role to default."

        ctx.send(out)
    }
}
