package xyz.gnarbot.gnar.listeners

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.EventListener
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.db.OptionsRegistry

class VoiceListener : EventListener {
    override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildVoiceJoinEvent -> onGuildVoiceJoin(event)
            is GuildVoiceLeaveEvent -> onGuildVoiceLeave(event)
            is GuildVoiceMoveEvent -> onGuildVoiceMove(event)
        }
    }

    private fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (!Launcher.loaded) {
            return
        }

        if (event.member.user == event.jda.selfUser) {
            return
        }

        val guild = event.guild

        Launcher.players.getExisting(guild.idLong)?.let {
            if (!it.guild?.selfMember!!.voiceState!!.inVoiceChannel()) {
                Launcher.players.destroy(guild.idLong)
            } else if (it.isAlone()) {
                it.queueLeave()
            } else {
                it.cancelLeave()
            }
        }
    }

    private fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (!Launcher.loaded) {
            return
        }

        if (event.member.user == event.jda.selfUser) {
            return Launcher.players.destroy(event.guild.idLong)
        }

        val guild = event.guild

        Launcher.players.getExisting(guild.idLong)?.let {
            if (!it.guild?.selfMember!!.voiceState!!.inVoiceChannel()) {
                Launcher.players.destroy(guild.idLong)
            } else if (it.isAlone()) {
                it.queueLeave()
            }
        }
    }

    private fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (!Launcher.loaded) {
            return
        }

        if (event.member.user == event.jda.selfUser) {
            if (!event.guild.selfMember.voiceState!!.inVoiceChannel()) {
                return Launcher.players.destroy(event.guild.idLong)
            }

            if (event.channelJoined.id == event.guild.afkChannel?.id) {
                return Launcher.players.destroy(event.guild.idLong)
            }

            Launcher.players.getExisting(event.guild.idLong)?.let {
                val options = OptionsRegistry.ofGuild(event.guild)
                if (options.music.channels.isNotEmpty()) {
                    if (event.channelJoined.id !in options.music.channels) {
                        it.currentRequestChannel?.let { requestChannel ->
                            requestChannel.sendMessage("Cannot join `${event.channelJoined.name}`, it isn't one of the designated music channels.")
                                .queue()
                        }

                        return Launcher.players.destroy(event.guild.idLong)
                    }
                }

                it.moveAudioConnection(event.channelJoined)

                if (it.isAlone()) {
                    it.queueLeave()
                } else {
                    it.cancelLeave()
                }
            }
            return
        }

        val guild = event.guild

        Launcher.players.getExisting(guild.idLong)?.let {
            when {
                !event.guild.selfMember.voiceState!!.inVoiceChannel() -> Launcher.players.destroy(event.guild.idLong)
                it.isAlone() -> it.queueLeave()
                else -> it.cancelLeave()
            }
        }
    }
}
