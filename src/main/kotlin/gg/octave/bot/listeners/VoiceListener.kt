package gg.octave.bot.listeners

import gg.octave.bot.Launcher
import gg.octave.bot.db.OptionsRegistry
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.EventListener

class VoiceListener : EventListener {
    override fun onEvent(event: GenericEvent) {
        if (event !is GenericGuildVoiceEvent || !Launcher.loaded) {
            return
        }

        when (event) {
            is GuildVoiceJoinEvent -> onGuildVoiceJoin(event)
            is GuildVoiceLeaveEvent -> onGuildVoiceLeave(event)
            is GuildVoiceMoveEvent -> onGuildVoiceMove(event)
        }
    }

    private fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.member.user == event.jda.selfUser) {
            return
        }

        checkVoiceState(event.guild)
    }

    private fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member.user == event.jda.selfUser) {
            return Launcher.players.destroy(event.guild.idLong)
        }

        checkVoiceState(event.guild)
    }

    private fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.member.user != event.jda.selfUser) {
            return
        }

        val manager = Launcher.players.getExisting(event.guild.idLong)
            ?: return

        if (event.channelJoined.id == event.guild.afkChannel?.id) {
            return Launcher.players.destroy(event.guild.idLong)
        }

        val options = OptionsRegistry.ofGuild(event.guild)

        if (options.music.channels.isNotEmpty() && event.channelJoined.id !in options.music.channels) {
            manager.currentRequestChannel
                ?.sendMessage("Cannot join `${event.channelJoined.name}`, it isn't one of the designated music channels.")
                ?.queue()

            return Launcher.players.destroy(event.guild.idLong)
        }

        checkVoiceState(event.guild)
    }

    private fun checkVoiceState(guild: Guild) {
        val manager = Launcher.players.getExisting(guild.idLong)
            ?: return

        if (!guild.selfMember.voiceState!!.inVoiceChannel()) {
            return Launcher.players.destroy(guild.idLong)
        }

        val guildData = Launcher.database.getGuildData(guild.id)
        val avoidLeave = guildData?.isPremium?.and(guildData.music.isAllDayMusic) ?: false
        when {
            manager.isAlone() && !manager.leaveQueued && !avoidLeave -> manager.queueLeave()
            !manager.isAlone() && manager.leaveQueued -> manager.cancelLeave()
        }
    }
}
