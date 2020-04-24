package xyz.gnarbot.gnar.commands.music.search

import com.jagrosh.jdautilities.menu.Selector
import com.jagrosh.jdautilities.menu.SelectorBuilder
import net.dv8tion.jda.api.EmbedBuilder
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.music.MusicLimitException
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.music.TrackScheduler
import xyz.gnarbot.gnar.utils.desc
import xyz.gnarbot.gnar.utils.getDisplayValue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Command(
        aliases = ["play", "p"],
        usage = "[url|YT search]",
        description = "Plays music in a voice channel"
)
@BotInfo(
        id = 62,
        scope = Scope.VOICE,
        category = Category.MUSIC
)
class PlayCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val botChannel = context.selfMember.voiceState?.channel
        val userChannel = context.voiceChannel

        if (botChannel != null && botChannel != userChannel) {
            context.send().issue("The bot is already playing music in another channel.").queue()
            return
        }
        val manager = context.bot.players.getExisting(context.guild)

        if (args.isEmpty()) {
            if (manager == null) {
                context.send().issue("There's no music player in this guild.\n" +
                        "\uD83C\uDFB6` ${config.prefix}play (song/url)` to start playing some music!").queue()
                return
            }

            when {
                manager.player.isPaused -> {
                    manager.player.isPaused = false
                    context.send().embed("Play Music") {
                        desc { "Music is no longer paused." }
                    }.action().queue()
                }
                manager.player.playingTrack != null -> {
                    context.send().error("Music is already playing. Are you trying to queue a track? Try adding a search term with this command!").queue()
                }
                manager.scheduler.queue.isEmpty() -> {
                    context.send().embed("Empty Queue") {
                        desc { "There is no music queued right now. Add some songs with `${config.prefix}play -song|url`." }
                    }.action().queue()
                }
            }
            return
        }

        prompt(context, manager).whenComplete { _, _ ->
            if(context.data.music.isVotePlay) {
                val newManager = try {
                    context.bot.players.get(context.guild)
                } catch (e: MusicLimitException) {
                    e.sendToContext(context)
                    return@whenComplete
                }

                startPlayVote(context, newManager, args, false, "")
            } else {
                play(context, args, false, "")
            }

        }
    }

    private fun prompt(context: Context, manager: MusicManager?) : CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        val oldQueue = TrackScheduler.getQueueForGuild(context.guild.id)
        if(manager == null && !oldQueue.isEmpty()) {
            SelectorBuilder(context.bot.eventWaiter)
                    .setType(Selector.Type.MESSAGE)
                    .title { "Would you like to keep your old queue?" }
                    .description { "Thanks for using Octave!" }
                    .addOption("Yes, keep it.") {
                        future.completeAsync {
                            context.send().info("Kept old queue. Playing new song first and continuing with your queue...").queue()
                            null
                        }
                    }.addOption("No, start a new queue.") {
                        future.completeAsync {
                            oldQueue.clear()
                            context.send().info("Scrapped old queue. A new queue will start.")
                            null
                        }
                    }.build().display(context.textChannel)
        } else {
            future.completeAsync { null }
        }

        return future
    }

    companion object {
        fun play(context: Context, args: Array<String>, isSearchResult: Boolean, uri: String) {
            val config = context.bot.configuration
            val manager = try {
                context.bot.players.get(context.guild)
            } catch (e: MusicLimitException) {
                e.sendToContext(context)
                return
            }

            //Reset expire time if play has been called.
            manager.scheduler.queue.clearExpire()

            if ("https://" in args[0] || "http://" in args[0] || args[0].startsWith("spotify:")) {
                val link = args[0].removePrefix("<").removeSuffix(">")

                manager.loadAndPlay(
                        context,
                        link,
                        TrackContext(
                                context.member.user.idLong,
                                context.textChannel.idLong
                        ), "You can search and pick results using ${config.prefix}youtube or ${config.prefix}soundcloud while in a channel.")
            } else if (isSearchResult) { //As in, it comes from SoundcloudCommand or YoutubeCommand
                manager.loadAndPlay(
                        context,
                        uri,
                        TrackContext(
                                context.member.user.idLong,
                                context.textChannel.idLong
                        )
                )
            } else {
                if (!context.bot.configuration.searchEnabled) {
                    context.send().issue("Search is currently disabled. Try direct links instead.").queue()
                    return
                }

                val query = args.joinToString(" ").trim()
                manager.loadAndPlay(
                        context,
                        "ytsearch:$query",
                        TrackContext(
                                context.member.user.idLong,
                                context.textChannel.idLong
                        ), "You can search and pick results using ${config.prefix}youtube or ${config.prefix}soundcloud while in a channel.")
            }
        }

        fun startPlayVote(context: Context, manager: MusicManager, args: Array<String>, isSearchResult: Boolean, uri: String) {
            if (manager.isVotingToPlay) {
                context.send().issue("There is already a vote going on!").queue()
                return
            }

            val voteSkipCooldown = if(context.data.music.votePlayCooldown <= 0) {
                context.bot.configuration.votePlayCooldown.toMillis()
            } else {
                context.data.music.votePlayCooldown
            }

            if (System.currentTimeMillis() - manager.lastPlayVoteTime < voteSkipCooldown) {
                context.send().issue("You must wait $voteSkipCooldown before starting a new vote.").queue()
                return
            }

            val voteSkipDuration = if(context.data.music.votePlayDuration == 0L) {
                context.data.music.votePlayDuration
            } else {
                context.bot.configuration.votePlayDuration.toMillis()
            }

            val votePlayDurationText = if(context.data.music.votePlayDuration == 0L) {
                context.bot.configuration.votePlayDurationText
            } else {
                getDisplayValue(context.data.music.votePlayDuration)
            }

            manager.lastPlayVoteTime = System.currentTimeMillis()
            manager.isVotingToPlay = true
            val halfPeople = context.selfMember.voiceState!!.channel!!.members.filter { member -> !member.user.isBot  }.size / 2

            context.send().embed("Vote Play") {
                desc {
                    buildString {
                        append(context.message.author.asMention)
                        append(" has voted to **play** the current track!")
                        append(" React with :thumbsup: or :thumbsdown:\n")
                        append("Whichever has the most votes in $votePlayDurationText will win! This requires at least $halfPeople on the VC to vote to skip.")
                    }
                }
            }.action()
                    .submit()
                    .thenCompose { m ->
                        m.addReaction("👍")
                                .submit()
                                .thenCompose { m.addReaction("👎").submit() }
                                .thenApply { m }
                    }
                    .thenCompose {
                        it.editMessage(EmbedBuilder(it.embeds[0])
                                .apply {
                                    desc { "Voting has ended! Check the newer messages for results." }
                                    clearFields()
                                }.build()
                        ).submitAfter(voteSkipDuration, TimeUnit.MILLISECONDS)
                    }.thenAccept { m ->
                        val skip = m.reactions.firstOrNull { it.reactionEmote.name == "👍" }?.count?.minus(1) ?: 0
                        val stay = m.reactions.firstOrNull { it.reactionEmote.name == "👎" }?.count?.minus(1) ?: 0

                        context.send().embed("Vote Skip") {
                            desc {
                                buildString {
                                    if (skip > halfPeople) {
                                        appendln("The vote has passed! The song will be queued.")
                                        play(context, args, isSearchResult, uri)
                                    } else {
                                        appendln("The vote has failed! The song will not be queued.")
                                    }
                                }
                            }
                            field("Results") {
                                "__$skip Play Votes__ — __$stay No Play Votes__"
                            }
                        }.action().queue()
                    }
                    .whenComplete { _, _ ->
                        manager.isVotingToPlay = false
                    }
        }
    }
}
