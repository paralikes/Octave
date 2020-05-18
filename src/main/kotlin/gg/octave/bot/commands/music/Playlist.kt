package gg.octave.bot.commands.music

import com.jagrosh.jdautilities.paginator
import gg.octave.bot.Launcher
import gg.octave.bot.db.music.MusicPlaylist
import gg.octave.bot.music.TrackContext
import gg.octave.bot.utils.extensions.*
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand
import me.devoxin.flight.api.entities.Cog

class Playlist : Cog {
    @Command(aliases = ["pl"], description = "Save or load a playlist.")
    fun playlist(ctx: Context) {
        ctx.send("Try load, save, list or delete.")
    }

    @SubCommand(description = "Lists all your playlists.")
    fun list(ctx: Context) {
        val playlists = ctx.db.getPlaylists(ctx.author.id)?.toList()
            ?: emptyList()

        ctx.textChannel?.let {
            Launcher.eventWaiter.paginator {
                setUser(ctx.author)
                title { "Playlists" }
                color { ctx.selfMember?.color }
                empty { "**No playlists saved.** \nSave a playlist with `${ctx.config.prefix}playlist save`." }
                finally { message -> message?.delete()?.queue() }

                for (name in playlists.map { dat -> dat.name }) {
                    addEntry(name)
                }
            }.display(it)
        }
    }

    @SubCommand(aliases = ["l"], description = "Load a playlist.")
    fun load(ctx: Context, name: String) {
        val id = "${ctx.author.id}:$name"
        val playlist = ctx.db.getPlaylist(id)?.toLavaPlaylist()
            ?: return ctx.send("Playlist with the name **$name** could not be found.")

        val trackContext = TrackContext(ctx.author.idLong, ctx.textChannel!!.idLong)
        val resultHandler = ctx.manager.MusicManagerAudioLoadResultHandler(ctx, id, trackContext, isNext = false)

        resultHandler.playlistLoaded(playlist)
    }

    @SubCommand(aliases = ["s"], description = "Save a playlist.")
    fun save(ctx: Context, name: String) {
        val queue = ctx.manager.scheduler.queue.toList()
        if (queue.count() == 0) {
            return ctx.send("Playlist is empty.")
        }

        MusicPlaylist("${ctx.author.id}:$name")
            .replacePlaylist(queue)
            .setName(name)
            .save()

        ctx.send("Playlist saved.")
    }

    @SubCommand(aliases = ["d"], description = "Delete a playlist.")
    fun delete(ctx: Context, name: String) {
        ctx.db.getPlaylist("${ctx.author.id}:$name")?.delete()
            ?: ctx.send("Playlist not found.")

        ctx.send("Playlist deleted.")
    }
}