package com.github.gltchitm.pongccheatstcpauthserver

import io.javalin.Javalin
import io.javalin.core.util.FileUtil
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.security.RouteRole
import io.javalin.http.staticfiles.Location
import io.javalin.http.SameSite

import org.eclipse.jetty.http.HttpStatus

import java.sql.*
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.io.File

import de.mkammerer.argon2.Argon2Factory
import com.xenomachina.argparser.ArgParser

val database: Connection? = DriverManager.getConnection("jdbc:sqlite:app/resources/database.sqlite3")

val verificationTokenFilename = "pongccheatstcpauthserver-verification-token-" + UUID.randomUUID().toString()
var verificationToken = UUID.randomUUID().toString()
val vertificationTokenFile = File("/tmp", verificationTokenFilename)

val argon2 = Argon2Factory.create()

val ARGON2_ITERATIONS = 10
val ARGON2_MEMORY = 65536
val ARGON2_PARALLELISM = 1

val COOKIE_NAME = "pongccheatstcpauthserver-web-token"
val COOKIE_PATH = "/"
val COOKIE_MAX_AGE = 28800
val COOKIE_SECURE = false
val COOKIE_VERSION = 0
val COOKIE_HTTP_ONLY = true
val COOKIE_SAME_SITE = SameSite.STRICT

enum class Role : RouteRole {
    EVERYONE,
    AUTHENTICATED,
    UNAUTHENTICATED
}

fun main(args: Array<String>) {
    val parsedArgs = ArgParser(args).parseInto(::Args)

    if (!parsedArgs.disableSignups) {
        vertificationTokenFile.writeText(verificationToken + "\n")
        vertificationTokenFile.deleteOnExit()
    }

    database!!.createStatement().executeUpdate(
        """
            CREATE TABLE IF NOT EXISTS users (
                user_id CHAR(36) PRIMARY KEY NOT NULL,
                username TEXT NOT NULL,
                password CHAR(97) NOT NULL
            );
            CREATE TABLE IF NOT EXISTS web_tokens (
                web_token CHAR(36) PRIMARY KEY NOT NULL,
                user_id CHAR(36) NOT NULL,
                expires INTEGER NOT NULL
            );
        """
    )

    val app = Javalin.create { config ->
        config.accessManager { handler, ctx, routeRoles ->
            val routeRole = routeRoles.toTypedArray().let {
                if (it.size > 0) {
                    it[0]
                } else {
                    Role.EVERYONE
                }
            }

            if (routeRole != Role.EVERYONE) {
                if (userFromWebToken(ctx.cookie(COOKIE_NAME).orEmpty()) != null) {
                    if (routeRole == Role.UNAUTHENTICATED) {
                        ctx.redirect("portal", HttpStatus.TEMPORARY_REDIRECT_307)
                        return@accessManager
                    }
                } else {
                    if (routeRole == Role.AUTHENTICATED) {
                        ctx.redirect("login", HttpStatus.TEMPORARY_REDIRECT_307)
                        return@accessManager
                    }
                }
            }

            handler.handle(ctx)
        }
    }.start(13949)

    app.routes {
        if (!parsedArgs.disableWebPortal) {
            get(WebController::root, Role.UNAUTHENTICATED)
            get("login", WebController::login, Role.UNAUTHENTICATED)
            if (!parsedArgs.disableSignups) {
                get("signup", WebController::signup, Role.UNAUTHENTICATED)
            } else {
                get("signup", WebController::signupsDisabled)
            }
            get("portal", WebController::portal, Role.AUTHENTICATED)
        } else {
            get(WebController::webPortalDisabled)
            get("login", WebController::webPortalDisabled)
            get("signup", WebController::webPortalDisabled)
            get("portal", WebController::webPortalDisabled)
        }
        path("api") {
            path("web") {
                if (!parsedArgs.disableWebPortal) {
                    post("login", WebApiController::login)
                    if (!parsedArgs.disableSignups) {
                        post("signup", WebApiController::signup)
                    }
                    put("change_password", WebApiController::changePassword)
                    delete("logout", WebApiController::logout)
                    delete("revoke_all_sessions", WebApiController::revokeAllSessions)
                    delete("delete_account", WebApiController::deleteAccount)
                }
            }
            path("client") {
                post("login", ClientController::login)
            }
            path("server") {
                post("redeem_token", ServerController::redeemToken)
            }
        }
    }

    startJanitor()
}
