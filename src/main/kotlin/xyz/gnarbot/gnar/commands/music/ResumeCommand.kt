package xyz.gnarbot.gnar.commands.music

import com.fasterxml.jackson.databind.ObjectMapper
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.db.Database
import xyz.gnarbot.gnar.music.MusicManager
import xyz.gnarbot.gnar.utils.PlaylistUtils

@Command(
        aliases = ["resume"],
        description = "Resumes the queue from before the bot was restarted."
)
@BotInfo(
        id = 476,
        category = Category.MUSIC,
        scope = Scope.VOICE,
        djLock = true
)
class ResumeCommand : MusicCommandExecutor(true, false, true) {
    var pool = Database.getDefaultJedisPool()
    var objectMapper = ObjectMapper()

    override fun execute(context: Context, label: String, args: Array<String>, manager: MusicManager) {
        pool.resource.use {
            val plId = "playlist:" + context.guild.id
            val pl = it.get(plId)

            if (pl.isNullOrEmpty()) {
                context.send().error("There's no playlist to be seen here.").queue()
                return
            }

            val scheduler = context.bot.players.get(context.guild).scheduler
            val encodedPlaylist: ArrayList<String> = objectMapper.readValue(pl, ArrayList::class.javaObjectType) as ArrayList<String>
            val playlist = PlaylistUtils.decodePlaylist(encodedPlaylist, "Processed Playlist");

            val tracks = playlist.tracks
            for (track in tracks)
                scheduler.queue(track)

            context.send().embed("Music Queue") {
                desc {
                    buildString {
                        append("Added `${encodedPlaylist.size}` tracks to queue from playlist `${playlist.name}`.\n")
                    }
                }
            }

            //Delete the playlist from redis.
            it.del(plId)
        }
    }
}
