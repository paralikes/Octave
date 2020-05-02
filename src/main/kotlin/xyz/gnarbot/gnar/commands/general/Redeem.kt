package xyz.gnarbot.gnar.commands.general

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import xyz.gnarbot.gnar.db.OptionsRegistry
import xyz.gnarbot.gnar.db.PremiumKey
import xyz.gnarbot.gnar.db.Redeemer
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.db
import java.awt.Color
import java.util.*

class Redeem : Cog {
    @Command(description = "Redeems a premium key.")
    fun redeem(ctx: Context, keyString: String) {
        if (keyString.isEmpty()) {
            return ctx.send("You need to give me a key to redeem.")
        }

        val key = ctx.db.getPremiumKey(keyString) ?: return ctx.send("That's not a valid code.")

        if (key.redeemer != null) {
            return ctx.send("That code has been redeemed already.")
        }

        when (key.type) {
            PremiumKey.Type.PREMIUM -> {
                key.setRedeemer(Redeemer(Redeemer.Type.GUILD, ctx.guild!!.id)).save()
                ctx.data.addPremiumKey(key.id, key.duration).save()
            }
            PremiumKey.Type.PREMIUM_OVERRIDE -> {
                key.setRedeemer(Redeemer(Redeemer.Type.PREMIUM_OVERRIDE, ctx.author.id)).save()
                OptionsRegistry.ofUser(ctx.author).addPremiumKey(key.id, key.duration).save()
            }
            else -> return ctx.send("Unknown key type.")
        }

        ctx.send {
            setTitle("Premium Code")
            setColor(Color.ORANGE)
            setDescription("Redeemed key `${key.id}`. **Thank you for supporting the bot's development!*")
            addField("Key Type", key.type.name, true)
            addField(
                "Donator Perks",
                "• First access to new features.\n" +
                    "• Use the music bot during maximum music capacity.\n" +
                    "• More tracks and higher track length!",
                true
            )
        }
    }
}
