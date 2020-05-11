package com.jagrosh.jdautilities.menu

import com.jagrosh.jdautilities.waiter.EventWaiter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.RestAction
import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Paginator(
    waiter: EventWaiter,
    user: User?,
    title: String?,
    description: String?,
    color: Color?,
    fields: List<MessageEmbed.Field>,
    val emptyMessage: String?,
    val list: List<List<String>>,
    timeout: Long,
    unit: TimeUnit,
    finally: (Message?) -> Unit
) : Menu(waiter, user, title, description, color, fields, timeout, unit, finally) {
    val LEFT = "\u25C0"
    val STOP = "\u23F9"
    val RIGHT = "\u25B6"

    private val menuPermissions = setOf(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE)

    fun display(channel: TextChannel) {
        if (!channel.guild.selfMember.hasPermission(channel, menuPermissions)) {
            val joined = menuPermissions.joinToString("`, `", prefix = "`", postfix = "`")
            channel.sendMessage("Error: The bot requires the permissions $joined for pagination menus.").queue()
            return finally(null)
        }

        paginate(channel, 1)
    }

    fun display(message: Message) = display(message.textChannel)

    fun paginate(channel: TextChannel, page: Int) {
        if (list.isEmpty()) {
            return channel.sendMessage(renderEmptyPage()).queue()
        }

        val pageNum = page.coerceIn(1, list.size)
        val msg = renderPage(page)
        initialize(channel.sendMessage(msg), pageNum)
    }

    fun paginate(message: Message, page: Int) {
        if (list.isEmpty()) {
            message.editMessage(renderEmptyPage()).queue()
            return
        }

        val pageNum = page.coerceIn(1, list.size)
        val msg = renderPage(page)
        initialize(message.editMessage(msg), pageNum)
    }

    private fun addButtons(message: Message, directions: Boolean): CompletableFuture<Void> {
        return when (directions) {
            true -> {
                message.addReaction(LEFT).submit()
                    .thenCompose { message.addReaction(STOP).submit() }
                    .thenCompose { message.addReaction(RIGHT).submit() }
                    .thenAccept {}
            }
            false -> {
                message.addReaction(STOP).submit()
                    .thenAccept {}
            }
        }
    }

    private fun initialize(action: RestAction<Message>, page: Int) {
        action.submit()
            .thenCompose { m ->
                addButtons(m, list.size > 1)
                    .thenApply { m }
            }
            .thenAccept { message ->
                waiter.waitFor(MessageReactionAddEvent::class.java) {
                    val pageNew = when (it.reactionEmote.name) {
                        LEFT -> page - 1
                        RIGHT -> page + 1
                        STOP -> {
                            finally(message)
                            return@waitFor
                        }
                        else -> {
                            finally(message)
                            error("Internal pagination error")
                        }
                    }

                    it.reaction.removeReaction(it.user!!).queue()

                    if (pageNew != page) {
                        message?.editMessage(renderPage(pageNew))?.queue {
                            paginate(it, pageNew)
                        }
                    }
                }.predicate {
                    when {
                        it.messageIdLong != message?.idLong -> false
                        it.user!!.isBot -> false
                        user != null && it.user != user -> {
                            it.reaction.removeReaction(it.user!!).queue()
                            false
                        }
                        else -> when (it.reactionEmote.name) {
                            LEFT, STOP, RIGHT -> true
                            else -> false
                        }
                    }
                }.timeout(timeout, unit) {
                    //finally(message)
                }
            }
    }

    private fun renderPage(page: Int): Message {
        val pageNum = page.coerceIn(1, list.size)
        val items = list[pageNum - 1]
        val embedDescription = buildString {
            description?.let { append(it).append('\n').append('\n') }
            items.forEachIndexed { index, s ->
                append('`').append(index + 1 + (pageNum - 1) * list[0].size).append("` ")
                append(s).append('\n')
            }
        }

        val embed = EmbedBuilder().apply {
            setColor(color)
            setTitle(title)
            setDescription(embedDescription)
            super.fields.forEach { addField(it) }
            setFooter("Page $pageNum/${list.size}", null)
        }.build()

        return MessageBuilder().setEmbed(embed).build()
    }

    private fun renderEmptyPage(): Message {
        val embed = EmbedBuilder().apply {
            setColor(color)
            emptyMessage?.let(this::setDescription)
            super.fields.forEach { addField(it) }

        }.build()

        return MessageBuilder().setEmbed(embed).build()
    }
}
