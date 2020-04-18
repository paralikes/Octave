package xyz.gnarbot.gnar.utils.extensions

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import xyz.gnarbot.gnar.music.LpErrorTranslator

fun FriendlyException.friendlierMessage() = LpErrorTranslator.translate(this)
