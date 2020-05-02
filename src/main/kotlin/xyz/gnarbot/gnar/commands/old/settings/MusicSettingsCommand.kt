package xyz.gnarbot.gnar.commands.settings

import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description
import xyz.gnarbot.gnar.utils.toDuration
import java.lang.RuntimeException
import java.time.Duration

class MusicSettingsCommand : CommandTemplate() {
    @Description("Changes the vote queue cooldown.")


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
