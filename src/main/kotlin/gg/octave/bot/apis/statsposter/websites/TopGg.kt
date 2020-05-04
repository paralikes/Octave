package gg.octave.bot.apis.statsposter.websites

class TopGg(botId: String, auth: String) : Website(
    "TopGG",
    "https://top.gg/api/bots/$botId/stats",
    "server_count",
    auth
)
