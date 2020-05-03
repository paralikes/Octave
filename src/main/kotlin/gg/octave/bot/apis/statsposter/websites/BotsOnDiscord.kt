package gg.octave.bot.apis.statsposter.websites

class BotsOnDiscord(botId: String, auth: String) : Website(
    "BotsOnDiscord",
    "https://bots.ondiscord.xyz/bot-api/bots/$botId/guilds",
    "guildCount",
    auth
)
