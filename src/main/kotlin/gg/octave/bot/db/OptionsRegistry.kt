package gg.octave.bot.db

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import gg.octave.bot.Launcher
import gg.octave.bot.db.guilds.GuildData
import gg.octave.bot.db.guilds.UserData

object OptionsRegistry {
    fun ofGuild(guild: Guild) = ofGuild(guild.id)
    fun ofGuild(guildId: Long) = ofGuild(guildId.toString())
    fun ofGuild(guildId: String) = Launcher.db.getGuildData(guildId) ?: GuildData(guildId)

    fun ofUser(user: User) = Launcher.db.getUserData(user.id) ?: UserData(user.id)
}
