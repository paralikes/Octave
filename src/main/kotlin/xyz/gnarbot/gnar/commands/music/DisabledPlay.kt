package xyz.gnarbot.gnar.commands.music

import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.entities.Cog

class DisabledPlay : Cog {
    @Command(description = "Disabled Play")
    fun disabledPlay(ctx: Context) {
        ctx.send("Music is disabled due to YouTube causing issues. Please stay tuned. https://discord.gg/musicbot")
    }
}