package xyz.gnarbot.gnar.utils.extensions

import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.sharding.ShardManager
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.entities.Configuration
import xyz.gnarbot.gnar.db.Database
import xyz.gnarbot.gnar.db.guilds.GuildData
import xyz.gnarbot.gnar.db.premium.PremiumGuild
import xyz.gnarbot.gnar.music.MusicManager

val Context.db: Database
    get() = Bot.getInstance().db()

val Context.shardManager: ShardManager
    get() = this.jda.shardManager!!

val Context.data: GuildData
    get() = Bot.getInstance().options.ofGuild(guild!!)

val Context.premiumGuild: PremiumGuild?
    get() = db.getPremiumGuild(guild!!.id)

val Context.isGuildPremium: Boolean
    get() = premiumGuild != null

val Context.config: Configuration
    get() = Bot.getInstance().configuration

val Context.bot: Bot
    get() = Bot.getInstance()

val Context.manager: MusicManager
    get() = Bot.getInstance().players.get(this.guild)

val Context.voiceChannel : VoiceChannel?
    get() = member!!.voiceState?.channel

val Context.selfMember : Member?
    get() = guild!!.selfMember
