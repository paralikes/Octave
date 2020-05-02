package xyz.gnarbot.gnar.db

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.db.guilds.GuildData
import xyz.gnarbot.gnar.db.guilds.UserData

object OptionsRegistry {
    fun ofGuild(guild: Guild) = ofGuild(guild.id)
    fun ofGuild(guildId: Long) = ofGuild(guildId.toString())
    fun ofGuild(guildId: String) = Launcher.db.getGuildData(guildId) ?: GuildData(guildId)

    fun ofUser(user: User) = Launcher.db.getUserData(user.id) ?: UserData(user.id)
}
