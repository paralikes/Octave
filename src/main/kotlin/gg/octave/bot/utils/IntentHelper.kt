package gg.octave.bot.utils

import net.dv8tion.jda.api.requests.GatewayIntent

object IntentHelper {
    private val disabledIntents = listOf(
        GatewayIntent.GUILD_INVITES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.DIRECT_MESSAGE_TYPING,
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_MEMBERS,
        //GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGE_TYPING,
        GatewayIntent.GUILD_PRESENCES
    )

    // Basically everything except GUILD_MESSAGES, GUILD_VOICE_STATES, and GUILD_MESSAGE_REACTIONS.
    // Not actually sure if we need GUILD_MESSAGE_REACTIONS but I've left it in for the sake of vote-* commands.

    val allIntents = GatewayIntent.ALL_INTENTS
    val disabledIntentsInt = GatewayIntent.getRaw(disabledIntents)
    val enabledIntentsInt = allIntents and disabledIntentsInt.inv()
    val enabledIntents = GatewayIntent.getIntents(enabledIntentsInt)
}
