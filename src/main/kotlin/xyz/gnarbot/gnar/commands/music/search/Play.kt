package xyz.gnarbot.gnar.commands.music.search

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.EmbedBuilder
import xyz.gnarbot.gnar.Launcher
import xyz.gnarbot.gnar.music.MusicLimitException
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.utils.extensions.config
import xyz.gnarbot.gnar.utils.extensions.data
import xyz.gnarbot.gnar.utils.extensions.selfMember
import xyz.gnarbot.gnar.utils.getDisplayValue
import java.util.concurrent.TimeUnit

class Play : Cog {
    @Command(aliases = ["p"], description = "Plays music in a voice channel.")
    fun play(ctx: Context) {

    }

    companion object {
        fun play(ctx: Context, args: Array<String>, isSearchResult: Boolean, uri: String) {
            val manager = try {
                Launcher.players.get(ctx.guild)
            } catch (e: MusicLimitException) {
                return e.sendToContext(ctx)
            }

            val config = ctx.config

            //Reset expire time if play has been called.
            manager.scheduler.queue.clearExpire()

            if ("https://" in args[0] || "http://" in args[0] || args[0].startsWith("spotify:")) {
                val link = args[0].removePrefix("<").removeSuffix(">")

                manager.loadAndPlay(
                    ctx,
                    link,
                    TrackContext(
                        ctx.member!!.user.idLong,
                        ctx.textChannel!!.idLong
                    ), "You can search and pick results using ${config.prefix}youtube or ${config.prefix}soundcloud while in a channel.")
            } else if (isSearchResult) { //As in, it comes from SoundcloudCommand or YoutubeCommand
                manager.loadAndPlay(
                    ctx,
                    uri,
                    TrackContext(
                        ctx.member!!.user.idLong,
                        ctx.textChannel!!.idLong
                    )
                )
            } else {
                val query = args.joinToString(" ").trim()
                manager.loadAndPlay(
                    ctx,
                    "ytsearch:$query",
                    TrackContext(
                        ctx.member!!.user.idLong,
                        ctx.textChannel!!.idLong
                    ), "You can search and pick results using ${config.prefix}youtube or ${config.prefix}soundcloud while in a channel.")
            }
        }

        fun startPlayVote(ctx: Context, manager: MusicManager, args: Array<String>, isSearchResult: Boolean, uri: String) {
            if (manager.isVotingToPlay) {
                return ctx.send("There is already a vote going on!")
            }

            val data = ctx.data

            val voteSkipCooldown = if (data.music.votePlayCooldown <= 0) {
                ctx.config.votePlayCooldown.toMillis()
            } else {
                data.music.votePlayCooldown
            }

            if (System.currentTimeMillis() - manager.lastPlayVoteTime < voteSkipCooldown) {
                return ctx.send("You must wait $voteSkipCooldown before starting a new vote.")
            }

            val votePlayDuration = if (data.music.votePlayDuration == 0L) {
                data.music.votePlayDuration
            } else {
                ctx.config.votePlayDuration.toMillis()
            }

            val votePlayDurationText = if (data.music.votePlayDuration == 0L) {
                ctx.config.votePlayDurationText
            } else {
                getDisplayValue(data.music.votePlayDuration)
            }

            manager.lastPlayVoteTime = System.currentTimeMillis()
            manager.isVotingToPlay = true
            val halfPeople = ctx.selfMember!!.voiceState!!.channel!!.members.filter { !it.user.isBot  }.size / 2

            ctx.messageChannel.sendMessage(EmbedBuilder().apply {
                setTitle("Vote Play")
                setDescription(
                    buildString {
                        append(ctx.author.asMention)
                        append(" has voted to **play** the current track!")
                        append(" React with :thumbsup: or :thumbsdown:\n")
                        append("Whichever has the most votes in $votePlayDurationText will win! This requires at least $halfPeople on the VC to vote to skip.")
                    }
                )
            }.build())
                .submit()
                .thenCompose { m ->
                    m.addReaction("👍")
                        .submit()
                        .thenApply { m }
                }
                .thenCompose {
                    it.editMessage(EmbedBuilder(it.embeds[0])
                        .apply {
                            setDescription("Voting has ended! Check the newer messages for results.")
                            clearFields()
                        }.build()
                    ).submitAfter(votePlayDuration, TimeUnit.MILLISECONDS)
                }.thenAccept { m ->
                    val votes = m.reactions.firstOrNull { it.reactionEmote.name == "👍" }?.count?.minus(1) ?: 0

                    ctx.send {
                        setTitle("Vote Skip")
                        setDescription(
                            buildString {
                                if (votes > halfPeople) {
                                    appendln("The vote has passed! The song will be queued.")
                                    play(ctx, args, isSearchResult, uri)
                                } else {
                                    appendln("The vote has failed! The song will not be queued.")
                                }
                            }
                        )
                        addField("Results", "__$votes Play Votes__", false)
                    }
                }.whenComplete { _, _ ->
                    manager.isVotingToPlay = false
                }
        }
    }
}
