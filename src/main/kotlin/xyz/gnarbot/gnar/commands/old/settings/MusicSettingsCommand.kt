package xyz.gnarbot.gnar.commands.settings

import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
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

class MusicSettingsCommand : CommandTemplate() {
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
}
