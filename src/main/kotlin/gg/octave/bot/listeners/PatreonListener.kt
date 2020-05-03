package gg.octave.bot.listeners
//
//import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
//import net.dv8tion.jda.api.hooks.ListenerAdapter
//import gg.octave.bot.Bot
//import gg.octave.bot.utils.response.respond
//
//class PatreonListener(private val bot: Bot) : ListenerAdapter() {}
class PatreonListener
//    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
//        if (event.author.isBot) return
//
//        if (event.message.contentRaw != "_patreon" && event.message.contentRaw != "_patron") {
//            return
//        }
//
//        event.channel.respond().info("Looking for your pledge, this may take a minute...")
//            .submit()
//            .thenCompose { bot.patreon.fetchPledges() }
//            .thenAccept { pledges ->
//                val pledge = pledges.firstOrNull { it.discordId != null && it.discordId == event.author.idLong }
//                    ?: return@thenAccept event.channel.respond().info("No pledge.").queue()
//
//                // we have pledge, insert user into database
//
//                // if (pledge != null) {
//                //   processPledge(event.channel, pledge)
//                // } else {
//                //   event.channel.respond().issue("**Sorry! I don't recognize you as a patron.**\n" +
//                //                                 "You can support Octave's development on our __**[Patreon](https://www.patreon.com/octavebot)**__.\n" +
//                //                                 "If you are a patron, make sure you either link your Discord account or enter your Patreon name, ie. `_patron Bill Gates`").queue()
//                // }
//            }
//            .exceptionally {
//                event.channel.respond().error(it.localizedMessage).queue()
//                return@exceptionally null
//            }
//    }
//
//    private fun processPledge(channel: PrivateChannel, pledge: Pledge) {
//        val entry = bot.db().getPatreonEntry(pledge.patron.id)
//        if (entry != null) {
//            val claimedDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(entry.timeOfClaim), ZoneId.systemDefault())
//            sendKeys(channel, "You have already claimed your keys on `${claimedDate.format(DateTimeFormatter.RFC_1123_DATE_TIME)}`. \uD83D\uDC9F", entry.keys)
//        } else {
//            val keys = Array(pledge.reward.amountCents / 100) { createKey() }.toList()
//            sendKeys(channel, "Here are your brand new premium keys. \u2764", keys)
//            PatreonEntry(pledge.patron.id, System.currentTimeMillis(), keys).save()
//        }
//    }
//
//    private fun sendKeys(channel: PrivateChannel, message: String, keys: List<String>) {
//        channel.respond().embed("Premium Keys") {
//            desc { "$message\nRedeem them in your server using `_redeem (key)`." }
//            color { Color.ORANGE }
//            field("Keys") {
//                buildString {
//                    keys.forEach {
//                        append("• `")
//                        append(it)
//                        append("`\n")
//                    }
//                }
//            }
//        }.action().queue()
//    }
//
//    private fun createKey(): String {
//        val key = PremiumKey(UUID.randomUUID().toString(), PremiumKey.Type.PREMIUM, Duration.ofDays(365).toMillis())
//        key.save()
//        return key.id
//    }
//}

/* TODO */
