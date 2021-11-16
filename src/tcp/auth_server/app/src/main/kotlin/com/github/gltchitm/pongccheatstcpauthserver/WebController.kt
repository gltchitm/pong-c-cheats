package com.github.gltchitm.pongccheatstcpauthserver

import io.javalin.core.util.FileUtil
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus
import io.javalin.http.Cookie
import io.javalin.http.SameSite

object WebController {
    fun root(ctx: Context) {
        ctx.redirect("login", HttpStatus.PERMANENT_REDIRECT_308)
    }
    fun login(ctx: Context) {
        ctx.html(FileUtil.readFile("app/resources/login.html"))
    }
    fun signup(ctx: Context) {
        ctx.html(
            FileUtil
                .readFile("app/resources/signup.html")
                .replace("{{verificationTokenFilename}}", verificationTokenFilename)
        )
    }
    fun portal(ctx: Context) {
        ctx.html(
            FileUtil
                .readFile("app/resources/portal.html")
                .replace("{{username}}", userFromWebToken(ctx.cookie(COOKIE_NAME)!!)!!.username)
        )
    }
    fun signupsDisabled(ctx: Context) {
        ctx.html(FileUtil.readFile("app/resources/signupsDisabled.html"))
    }
    fun webPortalDisabled(ctx: Context) {
        ctx.html(FileUtil.readFile("app/resources/webPortalDisabled.html"))
    }
}
