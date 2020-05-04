package gg.octave.bot.utils.extensions

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import gg.octave.bot.music.LpErrorTranslator

fun FriendlyException.friendlierMessage() = LpErrorTranslator.translate(this)
