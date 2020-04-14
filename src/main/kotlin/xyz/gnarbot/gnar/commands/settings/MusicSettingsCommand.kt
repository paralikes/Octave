package xyz.gnarbot.gnar.commands.settings

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.VoiceChannel
import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description
import xyz.gnarbot.gnar.utils.toDuration
import java.lang.NumberFormatException
import java.lang.RuntimeException
import java.time.Duration

@Command(
        aliases = ["musicsettings", "musicconfig"],
        description = "Change music settings."
)
@BotInfo(
        id = 55,
        category = Category.SETTINGS,
        permissions = [Permission.MANAGE_SERVER]
)
class MusicSettingsCommand : CommandTemplate() {
    @Description("Toggle music announcement.")
    fun toggle_announcements(context: Context) {
        val value = context.data.music.announce
        context.data.music.announce = !value
        context.data.save()

        context.send().info(if (value) "Announcements for music disabled." else "Announcements for music enabled.").queue()
    }

    @Description("Add voice channels that Octave can play music in.")
    fun voice_channel_add(context: Context, channel: VoiceChannel) {
        if (channel.id in context.data.music.channels) {
            context.send().error("`${channel.name}` is already a music channel.").queue()
            return
        }

        if (channel == context.guild.afkChannel) {
            context.send().error("`${channel.name}` is the AFK channel, you can't play music there.").queue()
            return
        }

        context.data.music.channels.add(channel.id)
        context.data.save()

        context.send().info("`${channel.name}` is now a designated music channel.").queue()
    }

    @Description("Remove voice channels that Octave can play music in.")
    fun voice_channel_remove(context: Context, channel: VoiceChannel) {
        if (channel.id !in context.data.music.channels) {
            context.send().error("`${channel.name}` is not one of the music channels.").queue()
            return
        }

        context.data.music.channels.remove(channel.id)
        context.data.save()

        context.send().info("${channel.name} is no longer a designated music channel.").queue()
    }

    @Description("List all settings, their description and their values.")
    fun list(context: Context) {
        context.send().embed("Music Settings") {
            field("Announcements") {
                buildString {
                    append("Music announcements are __")
                    append(if (context.data.music.announce) "enabled" else "disabled")
                    append("__, toggle with `_music toggle announcements`.")
                }
            }

            field("Music Channels") {
                buildString {
                    append("If this is not empty, Octave will only play music in these voice channels.\n\n")
                    context.data.music.channels.let {
                        if (it.isEmpty()) append("None.")

                        it.mapNotNull(context.guild::getVoiceChannelById)
                                .map(VoiceChannel::getName)
                                .forEach { append("• ").append(it).append('\n') }
                    }
                }
            }
        }.action().queue()
    }

    @Description("Set the maximum song length")
    fun song_length(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.maxSongLength = 0
            context.data.save()

            context.send().info("Reset song length limit.").queue()
            return
        }

        val amount: Duration = try{
            content.toDuration()
        } catch (e: RuntimeException) {
            context.send().info("Wrong duration specified: Expected something like `40 minutes`").queue()
            return
        }

        var durationLimit = if(context.isGuildPremium) {
            context.premiumGuild.songLengthQuota
        } else {
            config.durationLimit.toMillis()
        }

        if(amount.toMillis() > durationLimit) {
            context.send().error("This is too much. The limit is ${config.durationLimitText}.").queue()
            return
        }

        if(amount.toMinutes() < 1) {
            context.send().error("That's too little. It has to be more than 1 minute.").queue()
            return
        }

        context.data.music.maxSongLength = amount.toMillis()
        context.data.save()
        context.send().info("Successfully set song length limit to $content.").queue()
    }

    @Description("Sets the maximum queue size")
    fun queue_size(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.maxQueueSize = 0
            context.data.save()

            context.send().info("Reset queue limit.").queue()
            return
        }

        val amount: Int = try {
            content.toInt()
        } catch (e: NumberFormatException) {
            context.send().error("You need to input a number from 1 to ${config.queueLimit}.").queue()
            return
        }

        var queueLimit = if(context.isGuildPremium) {
            context.premiumGuild.queueSizeQuota
        } else {
            config.queueLimit
        }

        if(amount > queueLimit) {
            context.send().error("This is too much. The limit is $queueLimit.").queue()
            return
        }

        if(amount < 2) {
            context.send().error("Has to be more than 2.").queue()
            return
        }

        context.data.music.maxQueueSize = amount
        context.data.save()
        context.send().info("Successfully set queue limit to $amount.").queue()
    }

    @Description("Enables the vote queue.")
    fun votequeue_enable(context: Context) {
        context.data.music.isVotePlay = true
        context.data.save()

        context.send().info("Enabled vote play.").queue()
    }

    @Description("Disables the vote queue.")
    fun votequeue_disable(context: Context) {
        context.data.music.isVotePlay = false
        context.data.save()

        context.send().info("Successfully disabled vote play.").queue()
    }

    @Description("Changes the vote queue cooldown.")
    fun votequeue_cooldown(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.votePlayCooldown = 0
            context.data.save()

            context.send().info("Reset vote play cooldown.").queue()
            return
        }

        val amount: Duration = try{
            content.toDuration()
        } catch (e: RuntimeException) {
            context.send().info("Wrong duration specified: Expected something like `40 minutes`").queue()
            return
        }

        if(amount > config.votePlayCooldown) {
            context.send().error("This is too much. The limit is ${config.votePlayCooldownText}.").queue()
            return
        }

        if(amount.toSeconds() < 10) {
            context.send().error("Has to be more than 10 seconds.").queue()
            return
        }

        context.data.music.votePlayCooldown = amount.toMillis()
        context.data.save()
        context.send().info("Successfully set vote play cooldown to $content.").queue()
    }

    @Description("Changes the vote queue duration.")
    fun votequeue_duration(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.votePlayDuration = 0
            context.data.save()

            context.send().info("Reset vote play duration.").queue()
            return
        }

        val amount: Duration = try{
            content.toDuration()
        } catch (e: RuntimeException) {
            context.send().info("Wrong duration specified: Expected something like `40 minutes`").queue()
            return
        }

        if(amount > config.votePlayDuration) {
            context.send().error("This is too much. The limit is ${config.votePlayDurationText}.").queue()
            return
        }

        if(amount.toSeconds() < 10) {
            context.send().error("Has to be more than 10 seconds.").queue()
            return
        }

        context.data.music.votePlayDuration = amount.toMillis()
        context.data.save()
        context.send().info("Successfully set vote play duration to $content.").queue()
    }

    @Description("Changes the vote skip cooldown.")
    fun voteskip_cooldown(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.voteSkipCooldown = 0
            context.data.save()

            context.send().info("Reset voteskip cooldown.").queue()
            return
        }

        val amount: Duration = try{
            content.toDuration()
        } catch (e: RuntimeException) {
            context.send().info("Wrong duration specified: Expected something like `40 minutes`").queue()
            return
        }

        if(amount > config.voteSkipCooldown) {
            context.send().error("This is too much. The limit is ${config.voteSkipCooldownText}.").queue()
            return
        }

        if(amount.toSeconds() < 10) {
            context.send().error("Has to be more than 10 seconds.").queue()
            return
        }

        context.data.music.voteSkipCooldown = amount.toMillis()
        context.data.save()
        context.send().info("Successfully set vote skip cooldown to $content.").queue()
    }

    @Description("Changes the vote skip duration.")
    fun voteskip_duration(context: Context, content: String) {
        if (content == "reset") {
            context.data.music.voteSkipDuration = 0
            context.data.save()

            context.send().info("Reset voteskip duration.").queue()
            return
        }

        val amount: Duration = try{
            content.toDuration()
        } catch (e: RuntimeException) {
            context.send().info("Wrong duration specified: Expected something like `40 minutes`").queue()
            return
        }

        if(amount > config.voteSkipDuration) {
            context.send().error("This is too much. The limit is ${config.voteSkipDurationText}.").queue()
            return
        }


        if(amount.toSeconds() < 10) {
            context.send().error("Has to be more than 10 seconds.").queue()
            return
        }

        context.data.music.voteSkipDuration = amount.toMillis()
        context.data.save()
        context.send().info("Successfully set vote skip duration to $content.").queue()
    }

    @Description("Set the DJ role")
    fun dj_role_set(context: Context, role: Role?) {
        if(role == null) {
            context.send().error("The role doesn't exist.").queue()
            return
        }

        context.data.command.djRole = role.id
        context.data.save()

        context.send().info("Successfully set the DJ Role to ${role.name} (${role.id}).").queue()
    }

    @Description("Reset the DJ role")
    fun dj_role_reset(context: Context) {
        context.data.command.djRole = null
        context.data.save()

        context.send().info("Successfully reset DJ role name.").queue()
    }
}
