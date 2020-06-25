package gg.octave.bot.commands.music

import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.manager
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.getDisplayValue
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import net.dv8tion.jda.api.EmbedBuilder
import java.util.concurrent.TimeUnit

class VoteSkip : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @Command(description = "Vote to skip the current music track.")
    suspend fun voteskip(ctx: Context) {
        val manager = ctx.manager

        if (ctx.member!!.voiceState!!.isDeafened) {
            return ctx.send("You actually have to be listening to the song to start a vote.")
        }

        if (manager.isVotingToSkip) {
            return ctx.send("There's already a vote going on.")
        }

        val data = ctx.data

        val voteSkipCooldown = if (data.music.voteSkipCooldown == 0L) {
            ctx.config.voteSkipCooldown.toMillis()
        } else {
            data.music.voteSkipCooldown
        }

        val voteSkipCooldownText = if (data.music.voteSkipCooldown == 0L) {
            ctx.config.voteSkipCooldownText
        } else {
            getDisplayValue(data.music.voteSkipCooldown)
        }

        if (System.currentTimeMillis() - manager.lastVoteTime < voteSkipCooldown) {
            return ctx.send("You must wait $voteSkipCooldownText before starting a new vote.")
        }

        val voteSkipDuration = if (data.music.voteSkipDuration == 0L) {
            ctx.config.voteSkipDuration.toMillis()
        } else {
            data.music.voteSkipDuration
        }

        val voteSkipDurationText = if (data.music.voteSkipDuration == 0L) {
            ctx.config.voteSkipDurationText
        } else {
            val durationMinutes = ctx.config.voteSkipDuration.toMinutes()
            if (durationMinutes > 0) {
                "$durationMinutes minutes"
            } else {
                "${ctx.config.voteSkipDuration.toSeconds()} seconds"
            }
        }

        if (manager.player.playingTrack.duration - manager.player.playingTrack.position <= voteSkipDuration) {
            return ctx.send("By the time the vote finishes in $voteSkipDurationText, the song will be over.")
        }

        manager.lastVoteTime = System.currentTimeMillis()
        manager.isVotingToSkip = true
        val halfPeople = ctx.selfMember!!.voiceState!!.channel!!.members.filterNot { it.user.isBot }.size / 2

        val message = ctx.sendAsync {
            setTitle("Vote Skip")
            setDescription(
                buildString {
                    append(ctx.message.author.asMention)
                    append(" has voted to **skip** the current track!")
                    append(" React with :thumbsup:\n")
                    append("If at least **${halfPeople + 1}** vote(s) from listeners are obtained " +
                        "within **$voteSkipDurationText**, the song will be skipped!")
                }
            )
        }

        message.addReaction("👍")
            .submit()
            .thenApply { message }
            .thenCompose {
                it.editMessage(EmbedBuilder(it.embeds[0])
                    .apply {
                        setDescription("Voting has ended! Check the newer messages for results.")
                        clearFields()
                    }.build()
                ).submitAfter(voteSkipDuration, TimeUnit.MILLISECONDS)
            }.thenAccept { m ->
                m.reactions.firstOrNull { it.reactionEmote.name == "👍" }?.retrieveUsers()?.takeAsync(halfPeople + 1)?.thenAccept { users ->
                    val skip = users?.count { it !== ctx.message.author } ?: 0

                    ctx.send {
                        setTitle("Vote Skip")
                        setDescription(
                            buildString {
                                if (skip > halfPeople) {
                                    appendln("The vote has passed! The song has been skipped.")
                                    manager.scheduler.nextTrack()
                                } else {
                                    appendln("The vote has failed! The song will stay.")
                                }
                            }
                        )
                        addField("Results", "__$skip Skip Votes__", false)
                    }
                }
            }.whenComplete { _, _ ->
                manager.isVotingToSkip = false
            }
    }
}
