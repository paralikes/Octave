package gg.octave.bot.commands.admin

import gg.octave.bot.Launcher
import gg.octave.bot.utils.LazyThreadPool
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.entities.Cog
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.util.concurrent.CompletableFuture

class Eval : Cog {
    private val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    @Command(description = "Evaluate Kotlin code.", developerOnly = true)
    fun eval(ctx: Context, @Greedy code: String) {
        val stripped = code.replace("^```\\w+".toRegex(), "").removeSuffix("```")

        val bindings = mapOf(
            "ctx" to ctx,
            "jda" to ctx.jda,
            "sm" to ctx.jda.shardManager!!,
            "bot" to Launcher
        )

        val bindString = bindings.map { "val ${it.key} = bindings[\"${it.key}\"] as ${it.value.javaClass.kotlin.qualifiedName}" }.joinToString("\n")
        val bind = engine.createBindings()
        bind.putAll(bindings)

        try {
            val result = engine.eval("$bindString\n$stripped", bind)
                ?: return ctx.message.addReaction("👌").queue()

            if (result is CompletableFuture<*>) {
                ctx.messageChannel.sendMessage("```\nCompletableFuture<Pending>```").queue { m ->
                    result.whenComplete { r, ex ->
                        val post = ex ?: r
                        m.editMessage("```\n$post```").queue()
                    }
                }
            } else {
                ctx.send("```\n${result.toString().take(1950)}```")
            }
        } catch (e: Exception) {
            ctx.send("An exception occurred.\n```\n${e.localizedMessage}```")
        }
    }
}