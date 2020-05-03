package gg.octave.bot.music

import me.devoxin.flight.api.Context
import java.awt.Color

class MusicLimitException : Exception() {
    fun sendToContext(ctx: Context) {
        ctx.send {
            setColor(Color.ORANGE)
            setTitle("Maximum Capacity")
            setDescription(
                buildString {
                    append("Music is currently at maximum capacity, please try again later.\n")
                    append("Please consider donating to our **[Patreon](https://www.patreon.com/octavebot)** ")
                    append("to help us with hosting costs.")
                }
            )
            addField(
                "Why am I seeing this?",
                buildString {
                    append("Our music feature has a limit on how many channels we can play to at once, so we can ")
                    append("keep our music quality high and our server healthy. Donating will allow you to ")
                    append("bypass the limit, help us expand and upgrade our processing power.")
                },
                false
            )
        }
    }
}
