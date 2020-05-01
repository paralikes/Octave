package xyz.gnarbot.gnar.commands.admin

import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.commands.template.CommandTemplate
import xyz.gnarbot.gnar.commands.template.annotations.Description
import xyz.gnarbot.gnar.db.PremiumKey
import xyz.gnarbot.gnar.db.Redeemer
import xyz.gnarbot.gnar.utils.Utils
import java.util.*

@Command(aliases = ["key", "keys"])
@BotInfo(
        id = 1,
        admin = true,
        category = Category.NONE
)
class PremiumKeyCommand : CommandTemplate() {
    @Description("Generate a premium key.")
    fun gen(context: Context, number: Int, type: PremiumKey.Type?, durationTxt: String?) {
        val duration = Utils.parseTime(durationTxt).takeIf { it >= 0 }
                ?: return context.send().error("Why is negative duration a check you have in an admin command" ).queue()

        val builder = buildString {
            (0 until number)
                    .map { PremiumKey(UUID.randomUUID().toString(), type, duration).apply(PremiumKey::save).id }
                    .forEach { appendln(it) }
        }

        context.user.openPrivateChannel()
                .flatMap { it.sendMessage("```\n$builder```") }
                .queue()
    }

    @Description("Revoke a premium key.")
    fun revoke(context: Context, idString: String) {
        val ids = idString.split(",\\s*|\n").filterNot(String::isEmpty)
        val joiner = StringJoiner("\n")

        for (id in ids) {
            val key = context.bot.db().getPremiumKey(id)
            joiner.add("**Key** `$id`")
            if (key == null) {
                joiner.add("Doesn't exist in the database.\n")
                continue
            }

            val redeemer = key.redeemer
            if (redeemer != null) {
                when (redeemer.type) {
                    Redeemer.Type.GUILD -> {
                        val guildData = context.bot.db().getGuildData(redeemer.id)
                        if (guildData != null) {
                            guildData.premiumKeys.remove(key.id)
                            guildData.save()
                            joiner.add("Revoked the key from guild ID `${guildData.id}`.")
                        } else {
                            joiner.add("Guild ID `${redeemer.id}` redeemed the key but no longer exists in the DB.")
                        }
                    }
                    else -> joiner.add("Unknown redeemer type.")
                }
            } else {
                joiner.add("Not redeemed.")
            }

            key.delete()
            joiner.add("Deleted from the database.\n")
        }

        context.send().info(joiner.toString()).queue()
    }
}
