package gg.octave.bot.utils.extensions

import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.sharding.ShardManager
import gg.octave.bot.Launcher
import gg.octave.bot.entities.Configuration
import gg.octave.bot.db.Database
import gg.octave.bot.db.OptionsRegistry
import gg.octave.bot.db.guilds.GuildData
import gg.octave.bot.db.premium.PremiumGuild
import gg.octave.bot.music.MusicManager

val Context.db: Database
    get() = Launcher.database

val Context.shardManager: ShardManager
    get() = this.jda.shardManager!!

val Context.data: GuildData
    get() = OptionsRegistry.ofGuild(guild!!)

val Context.premiumGuild: PremiumGuild?
    get() = db.getPremiumGuild(guild!!.id)

val Context.isGuildPremium: Boolean
    get() = premiumGuild != null

val Context.config: Configuration
    get() = Launcher.configuration

val Context.launcher: Launcher
    get() = Launcher

val Context.manager: MusicManager
    get() = Launcher.players.get(this.guild)

val Context.voiceChannel : VoiceChannel?
    get() = member!!.voiceState?.channel

val Context.selfMember : Member?
    get() = guild!!.selfMember
