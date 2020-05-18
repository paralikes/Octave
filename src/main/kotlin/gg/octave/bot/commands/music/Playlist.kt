package gg.octave.bot.commands.music

import com.jagrosh.jdautilities.paginator
import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.search.Play
import gg.octave.bot.db.music.MusicPlaylist
import gg.octave.bot.music.MusicManager
import gg.octave.bot.music.TrackContext
import gg.octave.bot.utils.Utils
import gg.octave.bot.utils.extensions.*
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog

class Playlist : Cog {
    @Command(aliases = ["pl"], description = "Save or load a playlist.")
    fun playlist(ctx: Context) {
        ctx.send("Try load or save.")
    }

    @SubCommand()
    fun load(ctx: Context, name: String) {
        val formattedName = "${ctx.author.id}:$name"
        val playlist = ctx.db.getPlaylist(formattedName)?.toLavaPlaylist()
            ?: return ctx.send("Playlist with the name $name could not be found.")

        val trackContext = TrackContext(ctx.author.idLong, ctx.textChannel!!.idLong)
        val resultHandler = ctx.manager.MusicManagerAudioLoadResultHandler(ctx, formattedName, trackContext, isNext = false)

        resultHandler.playlistLoaded(playlist!!)
        ctx.send("Playlist loaded.")
    }

    @SubCommand()
    fun save(ctx: Context, name: String) {
        val queue = ctx.manager.scheduler.queue.toList()
        if (queue.count() == 0) {
            return ctx.send("Playlist is empty.")
        }

        MusicPlaylist("${ctx.author.id}:$name")
            .replacePlaylist(queue)
            .save()

        ctx.send("Playlist saved.")
    }
}