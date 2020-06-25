package gg.octave.bot.commands.music.search

import com.jagrosh.jdautilities.menu.Selector
import com.jagrosh.jdautilities.menu.SelectorBuilder
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gg.octave.bot.Launcher
import gg.octave.bot.listeners.FlightEventAdapter
import gg.octave.bot.music.MusicLimitException
import gg.octave.bot.music.MusicManager
import gg.octave.bot.music.TrackContext
import gg.octave.bot.music.TrackScheduler
import gg.octave.bot.music.settings.AutoShuffleSetting
import gg.octave.bot.utils.extensions.*
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

        if (query == null) {
            return playArgless(ctx)
        }

        val args = query.split(" +".toRegex())
        val hasManager = Launcher.players.contains(ctx.guild!!.idLong)

        prompt(ctx, hasManager).handle { _, _ ->
            val newManager = try {
                Launcher.players.get(ctx.guild)
            } catch (e: MusicLimitException) {
                // I don't like these try/catches everywhere. They also have a slight impact on performance.
                // TODO: Figure out a better solution.
                return@handle e.sendToContext(ctx)
            }

            smartPlay(ctx, newManager, args, false, "")
        }.exceptionally {
            ctx.send("An error occurred!")
            it.printStackTrace()
            return@exceptionally
        }
    }

    private fun playArgless(ctx: Context) {
        val manager = Launcher.players.getExisting(ctx.guild)
            ?: return ctx.send("There's no music player in this guild.\n\uD83C\uDFB6 `${ctx.trigger}play (song/url)` to start playing some music!")

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
    }

    private fun prompt(ctx: Context, hasManager: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val oldQueue = TrackScheduler.getQueueForGuild(ctx.guild!!.id)

        if (!hasManager && !oldQueue.isEmpty()) {
            SelectorBuilder(Launcher.eventWaiter)
                .setType(Selector.Type.MESSAGE)
                .setTitle("Would you like to keep your old queue?")
                .setDescription("Thanks for using Octave!")
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
        fun smartPlay(ctx: Context, manager: MusicManager?, args: List<String>, isSearchResult: Boolean, uri: String, isNext: Boolean = false, shuffle: Boolean = false) {
            when {
                ctx.data.music.isVotePlay && !FlightEventAdapter.isDJ(ctx, false) -> startPlayVote(ctx, manager!!, args, isSearchResult, uri, isNext)
                else -> play(ctx, args, isSearchResult, uri, isNext, shuffle)
            }
        }

        fun play(ctx: Context, args: List<String>, isSearchResult: Boolean, uri: String, isNext: Boolean = false, shuffle: Boolean = false) {
            val manager = try {
                Launcher.players.get(ctx.guild)
            } catch (e: MusicLimitException) {
                return e.sendToContext(ctx)
            }

            val config = ctx.config

            //Reset expire time if play has been called.
            manager.scheduler.queue.clearExpire()

            val query = when {
                "https://" in args[0] || "http://" in args[0] || args[0].startsWith("spotify:") -> {
                    args[0].removePrefix("<").removeSuffix(">")
                }
                isSearchResult -> uri
                else -> "ytsearch:${args.joinToString(" ").trim()}"
            }

            val trackContext = TrackContext(ctx.author.idLong, ctx.textChannel!!.idLong)

            manager.loadAndPlay(
                ctx,
                query,
                trackContext,
                if (!isSearchResult) "You can search and pick results using ${config.prefix}youtube or ${config.prefix}soundcloud while in a channel." else null,
                isNext,
                shuffle,
                object : AudioLoadResultHandler {
                    override fun loadFailed(exception: FriendlyException?) {
                    }

                    override fun trackLoaded(track: AudioTrack?) {
                        if (ctx.manager.scheduler.autoShuffle == AutoShuffleSetting.ON && !isNext) {
                            ctx.manager.scheduler.shuffle()
                        }
                    }

                    override fun noMatches() {
                    }

                    override fun playlistLoaded(playlist: AudioPlaylist?) {
                        if (ctx.manager.scheduler.autoShuffle == AutoShuffleSetting.ON && !isNext) {
                            ctx.manager.scheduler.shuffle()
                        }
                    }
                }
            )
        }

        fun startPlayVote(ctx: Context, manager: MusicManager, args: List<String>, isSearchResult: Boolean, uri: String, isNext: Boolean) {
            if (manager.isVotingToPlay) {
                return ctx.send("There is already a vote going on!")
            }

            val data = ctx.data

            val votePlayCooldown = if (data.music.votePlayCooldown <= 0) {
                ctx.config.votePlayCooldown.toMillis()
            } else {
                data.music.votePlayCooldown
            }

            if (System.currentTimeMillis() - manager.lastPlayVoteTime < votePlayCooldown) {
                return ctx.send("You must wait $votePlayCooldown before starting a new vote.")
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
            val channel = ctx.selfMember!!.voiceState!!.channel ?: ctx.voiceChannel!!
            val halfPeople = channel.members.filter { !it.user.isBot }.size / 2

            ctx.messageChannel.sendMessage(EmbedBuilder().apply {
                setTitle("Vote Play")
                setDescription(
                    buildString {
                        append(ctx.author.asMention)
                        append(" has voted to **play** a track!")
                        append(" React with :thumbsup:\n")
                        append("If there are more than $halfPeople vote(s) within $votePlayDurationText, the track will be queued.")
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
                        setTitle("Vote Play")
                        setDescription(
                            buildString {
                                if (votes > halfPeople) {
                                    appendln("The vote has passed! The song will be queued.")
                                    play(ctx, args, isSearchResult, uri, isNext)
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
