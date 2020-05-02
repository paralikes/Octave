package xyz.gnarbot.gnar.commands.admin        val bindings = mapOf(
            "ctx" to ctx,


import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.entities.Cog
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import xyz.gnarbot.gnar.Bot
import java.util.concurrent.CompletableFuture

class Eval : Cog {
    private val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    @Command(description = "Evaluate Kotlin code.", developerOnly = true)
    fun eval(ctx: Context, @Greedy code: String) {
        if (code.isEmpty()) { // Can't remember if this is even possible with Flight lol
            return ctx.send("Code can not be empty.") // TODO: Context methods like .send().issue()
        }

        val bindings = mapOf(
            "ctx" to ctx,
            "jda" to ctx.jda,
            "sm" to ctx.jda.shardManager!!,
            "bot" to Bot.getInstance()
        )

        val bindString = bindings.map { "val ${it.key} = bindings[\"${it.key}\"] as ${it.value.javaClass.kotlin.qualifiedName}" }.joinToString("\n")
        val bind = engine.createBindings()
        bind.putAll(bindings)

        try {
            val result = engine.eval("$bindString\n$code", bind)
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