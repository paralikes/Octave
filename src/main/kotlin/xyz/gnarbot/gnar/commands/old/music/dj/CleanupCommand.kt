package xyz.gnarbot.gnar.commands.music.dj

import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.commands.music.PLAY_MESSAGE
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.utils.PlaylistUtils.toAudioTrack

@Command(
        aliases = ["cleanup", "cu"],
        description = "Clear songs based on a specific user, duplicates, or if a user left"
)
@BotInfo(
        id = 88,
        category = Category.MUSIC,
        djLock = true
)
class CleanupCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val manager = context.bot.players.getExisting(context.guild)
            ?: return context.send().issue("There's no music player in this guild.\n$PLAY_MESSAGE").queue()

        if (context.message.mentionedUsers.isEmpty() && args.isEmpty()) {
            return context.send()
                .issue("`cleanup <left/duplicates/exceeds/@user>`\n\n" +
                    "`left` - Removes tracks by users no longer in the voice channel\n" +
                    "`duplicates` - Removes copies of tracks that are already queued\n" +
                    "`exceeds` - Removes tracks that exceeds the given duration (e.g.: `4:05`)\n" +
                    "`@user` - Removes all tracks added by the mentioned user")
                .queue()
        }

        val purge = context.message.mentionedUsers.firstOrNull()?.id
            ?: args[0]

        val oldSize = manager.scheduler.queue.size

        when (purge) {
            "left" -> {
                // Return Boolean: True if track should be removed
                val predicate: (String) -> Boolean = check@{
                    val track = toAudioTrack(it)

                    val req = context.guild.getMemberById(track.getUserData(TrackContext::class.java)!!.requester)
                        ?: return@check true

                    return@check req.voiceState?.channel?.idLong != context.guild.selfMember.voiceState?.channel?.idLong
                }

                manager.scheduler.queue.removeIf(predicate)
            }
            "duplicates", "d", "dupes" -> {
                val tracks = mutableSetOf<String>()
                // Return Boolean: True if track should be removed (could not add to set: already exists).
                val predicate: (String) -> Boolean = {
                    val track = toAudioTrack(it)
                    !tracks.add(track.identifier)
                }
                manager.scheduler.queue.removeIf(predicate)
            }
            "exceeds", "longerthan", "duration", "time" -> {
                val duration = args.getOrNull(1)
                    ?: return context.send().error("You need to specify a duration. Example: `cleanup exceeds 4:05`").queue()

                val parts = duration.split(':').mapNotNull { it.toIntOrNull() }

                when (parts.size) {
                    3 -> { // Hours, Minutes, Seconds
                        val (hours, minutes, seconds) = parts
                        val durationMillis = (hours * 3600000) + (minutes * 60000) + (seconds * 1000)
                        manager.scheduler.queue.removeIf {
                            val track = toAudioTrack(it)
                            track.duration > durationMillis
                        }
                    }
                    2 -> { // Minutes, Seconds
                        val (minutes, seconds) = parts
                        val durationMillis = (minutes * 60000) + (seconds * 1000)
                        manager.scheduler.queue.removeIf {
                            val track = toAudioTrack(it)
                            track.duration > durationMillis
                        }
                    }
                    1 -> { // Seconds
                        val durationMillis = parts[0] * 1000
                        manager.scheduler.queue.removeIf {
                            val track = toAudioTrack(it)
                            track.duration > durationMillis
                        }
                    }
                    else -> {
                        return context.send().error("The duration needs to be formatted as `00:00`. Examples:\n" +
                            "`cleanup exceeds 35` - Removes tracks longer than 35 seconds\n" +
                            "`cleanup exceeds 01:20` - Removes tracks longer than 1 minute and 20 seconds\n" +
                            "`cleanup exceeds 01:30:00` - Removes tracks longer than 1 hour and 30 minutes.").queue()
                    }
                }
            }
            else -> {
                val userId = purge.toLongOrNull()
                    ?: return context.send().issue("You need to mention a user, or pass a user ID.").queue()
                val predicate: (String) -> Boolean = {
                    val track = toAudioTrack(it)
                    track.getUserData(TrackContext::class.java)?.requester == userId
                }
                manager.scheduler.queue.removeIf(predicate)
            }
        }

        val newSize = manager.scheduler.queue.size
        val removed = oldSize - newSize

        when (purge) {
            "left" -> {
                if (removed == 0) {
                    return context.send().error("There are no songs to clear.").queue()
                }

                context.send().info("Removed $removed songs from users no longer in the voice channel.").queue()
            }
            "duplicates", "d", "dupes" -> {
                if (removed == 0) {
                    return context.send().error("There were no duplicates.").queue()
                }

                context.send().info("Removed $removed duplicate songs from the queue.").queue()
            }
            "exceeds", "longerthan", "duration", "time" -> {
                if (removed == 0) {
                    return context.send().error("There were no tracks that exceeded the given duration.").queue()
                }

                context.send().info("Removed $removed tracks from the queue.").queue()
            }
            else -> {
                val user = context.guild.getMemberById(purge)?.user?.name ?: "Unknown User"
                context.send().info("Removed $removed songs queued by **$user**.").queue()
            }
        }
    }
}
