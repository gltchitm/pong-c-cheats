package com.github.gltchitm.pongccheatstcpauthserver

import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus

object ServerController {
    private data class Response(val message: String?)

    private data class RedeemTokenRequest(val token: String?)

    fun redeemToken(ctx: Context) {
        val (token) = ctx.bodyAsClass<RedeemTokenRequest>()

        if (token == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Token must be provided."))
        } else {
            if (TokenManager.redeemToken(token)) {
                ctx
                    .status(HttpStatus.OK_200)
                    .json(Response(null))
            } else {
                ctx
                    .status(HttpStatus.FORBIDDEN_403)
                    .json(Response("Invalid token."))
            }
        }
    }
}
