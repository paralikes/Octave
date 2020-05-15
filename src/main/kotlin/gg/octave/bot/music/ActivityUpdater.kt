package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.embedTitle
import net.dv8tion.jda.api.entities.Activity

class ActivityUpdater(private val bot: Launcher, private val guildId: String) : AudioEventAdapter() {
    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (!endReason.mayStartNext) {
            bot.shardManager.setActivityProvider {
                setActivityForGuild(bot.configuration.game.format(it), it)
            }
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        bot.shardManager.setActivityProvider {
            setActivityForGuild(track.info.embedTitle, it)
        }
    }

    private fun setActivityForGuild(text: String, shardId: Int): Activity? {
        // this will not work properly with more than one server per shard
        val shard = bot.shardManager.getShardById(shardId)!!
        val hasGuild = shard.guilds.any { guild -> guild.id == guildId }

        return if (hasGuild) Activity.playing(text)
        else shard.presence.activity
    }
}