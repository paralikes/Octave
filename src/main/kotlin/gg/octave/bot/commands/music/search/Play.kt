package gg.octave.bot.commands.music.search

import com.jagrosh.jdautilities.menu.Selector
import com.jagrosh.jdautilities.menu.SelectorBuilder
import gg.octave.bot.Launcher
import gg.octave.bot.listeners.FlightEventAdapter
import gg.octave.bot.music.MusicLimitException
import gg.octave.bot.music.MusicManager
import gg.octave.bot.music.TrackContext
import gg.octave.bot.music.TrackScheduler
import gg.octave.bot.utils.extensions.config
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.selfMember
import gg.octave.bot.utils.extensions.voiceChannel
import gg.octave.bot.utils.getDisplayValue
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.EmbedBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Play : Cog {
    @Command(aliases = ["p"], description = "Plays music in a voice channel.")
    fun play(ctx: Context, @Greedy query: String?) {
        val botChannel = ctx.selfMember!!.voiceState?.channel
        val userChannel = ctx.voiceChannel

        if (botChannel != null && botChannel != userChannel) {
            return ctx.send("The bot is already playing music in another channel.")
        }

        val manager = Launcher.players.getExisting(ctx.guild)

        if (query == null) {
            if (manager == null) {
                return ctx.send("There's no music player in this guild.\n\uD83C\uDFB6` ${ctx.trigger}play (song/url)` to start playing some music!")
            }

            when {
                manager.player.isPaused -> {
                    manager.player.isPaused = false

                    ctx.send {
                        setTitle("Play Music")
                        setDescription("Music is no longer paused.")
                    }
                }
                manager.player.playingTrack != null -> {
                    ctx.send("Music is already playing. Are you trying to queue a track? Try adding a search term with this command!")
                }
                manager.scheduler.queue.isEmpty() -> {
                    ctx.send {
                        setTitle("Empty Queue")
                        setDescription("There is no music queued right now. Add some songs with `${ctx.trigger}play (song/url)`.")
                    }
                }
            }
            return
        }

        val args = query.split(" +".toRegex()).toTypedArray()

        prompt(ctx, manager).whenComplete { _, _ ->
            if (ctx.data.music.isVotePlay && !FlightEventAdapter.isDJ(ctx, false)) {
                val newManager = try {
                    Launcher.players.get(ctx.guild)
                } catch (e: MusicLimitException) {
                    return@whenComplete e.sendToContext(ctx)
                }

                startPlayVote(ctx, newManager, args, false, "")
            } else {
                play(ctx, args, false, "")
            }
        }
    }

    private fun prompt(ctx: Context, manager: MusicManager?): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        val oldQueue = TrackScheduler.getQueueForGuild(ctx.guild!!.id)
        if (manager == null && !oldQueue.isEmpty()) {
            SelectorBuilder(Launcher.eventWaiter)
                .setType(Selector.Type.MESSAGE)
                .title { "Would you like to keep your old queue?" }
                .description { "Thanks for using Octave!" }
                .addOption("Yes, keep it.") {
                    ctx.send("Kept old queue. Playing new song first and continuing with your queue...")
                    future.complete(null)
                }.addOption("No, start a new queue.") {
                    oldQueue.clear()
                    ctx.send("Scrapped old queue. A new queue will start.")
                    future.complete(null)
                }.build().display(ctx.textChannel!!)
        } else {
            future.complete(null)
        }

        return future
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
            val halfPeople = ctx.selfMember!!.voiceState!!.channel!!.members.filter { !it.user.isBot }.size / 2

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
