package xyz.gnarbot.gnar.apis.statsposter.websites

import okhttp3.RequestBody
import okhttp3.Response
import xyz.gnarbot.gnar.utils.RequestUtil
import xyz.gnarbot.gnar.utils.extensions.assert
import java.util.concurrent.CompletableFuture

abstract class Website(
    val name: String,
    private val postUrl: String,
    private val countHeader: String,
    private val authorization: String
) {

    fun canPost(): Boolean {
        return postUrl.isNotEmpty() && countHeader.isNotEmpty() && authorization.isNotEmpty()
    }

    fun update(count: Long): CompletableFuture<Response> {
        return RequestUtil.request {
            url(postUrl)
            post(RequestBody.create(RequestUtil.APPLICATION_JSON, "{\"$countHeader\": $count}"))
            header("Authorization", authorization)
        }.submit().assert(Response::isSuccessful) { "Posting to $name failed: ${it.code()} ${it.message()}" }
    }

}
