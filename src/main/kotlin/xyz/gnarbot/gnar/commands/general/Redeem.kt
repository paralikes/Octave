package xyz.gnarbot.gnar.commands.general

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.db.PremiumKey
import xyz.gnarbot.gnar.db.Redeemer
import xyz.gnarbot.gnar.utils.extensions.bot
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.db
import xyz.gnarbot.gnar.utils.field
import java.awt.Color
import java.util.*

class Redeem : Cog {
    @Command(description = "Redeems a premium key.")
    fun redeem(ctx: Context, keyString: String?) {
        if(keyString.isNullOrBlank())
            return ctx.send("You need to give me a key to redeem.")

        val key = ctx.db.getPremiumKey(keyString) ?: return ctx.send("That's not a valid code.")

        if(key.redeemer != null)
            return ctx.send("That code has been redeemed already.")

        when (key.type) {
            PremiumKey.Type.PREMIUM -> {
                key.setRedeemer(Redeemer(Redeemer.Type.GUILD, ctx.guild!!.id)).save()
                ctx.data.addPremiumKey(key.id, key.duration).save()

                ctx.send {
                    setTitle("Premium Code")
                    setColor(Color.ORANGE)
                    setDescription("Redeemed key `${key.id}`. **Thank you for supporting the bot's development!*")
                    field("**Key Type:** ${key.type}", true, StringJoiner("\n"))
                    field("Donator Perks", true, StringJoiner("\n")
                            .add("• First access to new features.")
                            .add("• Use the music bot during maximum music capacity.")
                            .add("More tracks and higher track length!")
                    )
                }
            }

            PremiumKey.Type.PREMIUM_OVERRIDE -> {
                key.setRedeemer(Redeemer(Redeemer.Type.PREMIUM_OVERRIDE, ctx.author.id)).save()
                ctx.bot.options.ofUser(ctx.author).addPremiumKey(key.id, key.duration).save()

                ctx.send {
                    setTitle("Premium Code")
                    setColor(Color.ORANGE)
                    setDescription("Redeemed key `${key.id}`. **Thank you for supporting the bot's development!**")
                    field("**Key Type:** ${key.type}", true, StringJoiner("\n"))
                    field("Donator Perks", true, StringJoiner("\n")
                            .add("• First access to new features.")
                            .add("• Use the music bot during maximum music capacity.")
                            .add("More tracks and higher track length!")
                    )
                }
            }

            else -> ctx.send("Unknown key type.")
        }
    }
}