package com.github.gltchitm.pongccheatstcpauthserver

import io.javalin.http.Context
import io.javalin.http.Cookie
import io.javalin.http.SameSite
import io.javalin.http.BadRequestResponse

import org.eclipse.jetty.http.HttpStatus

import java.util.UUID
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.sql.ResultSet

import de.mkammerer.argon2.Argon2Factory

import com.github.gltchitm.pongccheatstcpauthserver.WebApiController

import kotlin.text.toCharArray

object WebApiController {
    private data class Response(val message: String?)

    private data class LoginRequest(val username: String?, val password: String?)
    private data class SignupRequest(val username: String?, val password: String?, val verificationToken: String?)
    private data class ChangePasswordRequest(val oldPassword: String?, val newPassword: String?)

    fun login(ctx: Context) {
        val (username, password) = ctx.bodyAsClass<LoginRequest>()

        if (username == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Username must be provided."))
        } else if (password == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Password must be provided."))
        } else {
            if (username.length <= 32 && password.length <= 2048) {
                var statement = database!!.prepareStatement("SELECT * FROM users WHERE username = ?")
                statement.setString(1, username)

                val user = statement.executeQuery()

                if (user.next()) {
                    var passwordArray = password.toCharArray()
                    val passwordIsCorrect = argon2.verify(user.getString(3), passwordArray)

                    argon2.wipeArray(passwordArray)

                    if (passwordIsCorrect) {
                        if (argon2.needsRehash(user.getString(3), ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM)) {
                            statement = database.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")

                            passwordArray = password.toCharArray()

                            statement.setString(1, argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, passwordArray))

                            argon2.wipeArray(passwordArray)

                            statement.setString(2, user.getString(1))

                            statement.executeUpdate()
                        }

                        var webToken = UUID.randomUUID().toString()
                        var cookie = Cookie(
                            COOKIE_NAME,
                            webToken,
                            COOKIE_PATH,
                            COOKIE_MAX_AGE,
                            COOKIE_SECURE,
                            COOKIE_VERSION,
                            COOKIE_HTTP_ONLY,
                            null,
                            null,
                            COOKIE_SAME_SITE
                        )

                        statement = database.prepareStatement("INSERT INTO web_tokens VALUES (?, ?, ?)")
                        statement.setString(1, webToken)
                        statement.setString(2, user.getString(1))
                        statement.setLong(3, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8))

                        statement.executeUpdate()

                        ctx
                            .status(HttpStatus.OK_200)
                            .cookie(cookie)
                            .json(Response(null))

                        return
                    }
                }
            }

            ctx
                .status(HttpStatus.UNAUTHORIZED_401)
                .json(Response("Incorrect username or password."))
        }
    }
    fun signup(ctx: Context) {
        var statement = database!!.prepareStatement("SELECT COUNT(*) FROM users")
        val userCount = statement.executeQuery().getInt(1)

        val (username, password, verificationToken) = ctx.bodyAsClass<SignupRequest>()

        if (username == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Username must be provided."))
        } else if (password == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Password must be provided."))
        } else if (verificationToken == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Verification token must be provided."))
        } else if (Regex("[^a-zA-Z]").containsMatchIn(username)) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Username must be alphabetic."))
        } else if (username.length < 3) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Username must be at least 3 characters long."))
        } else if (username.length > 32) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Username cannot be longer than 32 characters."))
        } else if (password.length < 8) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Password must be at least 8 characters long."))
        } else if (password.length > 2048) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Password cannot be longer than 2048 characters."))
        } else if (verificationToken != com.github.gltchitm.pongccheatstcpauthserver.verificationToken) {
            ctx
                .status(HttpStatus.UNAUTHORIZED_401)
                .json(Response("Incorrect verification token."))
        } else if (userCount > 50) {
            ctx
                .status(HttpStatus.FORBIDDEN_403)
                .json(Response("Too many accounts exist."))
        } else {
            statement = database.prepareStatement("SELECT * FROM users WHERE username = ?")
            statement.setString(1, username)

            if (statement.executeQuery().next()) {
                ctx
                    .status(HttpStatus.CONFLICT_409)
                    .json(Response("Username is taken."))
            } else {
                com.github.gltchitm.pongccheatstcpauthserver.verificationToken = UUID.randomUUID().toString()
                vertificationTokenFile.writeText(com.github.gltchitm.pongccheatstcpauthserver.verificationToken + "\n")

                val passwordArray = password.toCharArray()

                statement = database.prepareStatement("INSERT INTO users VALUES (?, ?, ?)")
                statement.setString(1, UUID.randomUUID().toString())
                statement.setString(2, username)
                statement.setString(3, argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, passwordArray))
                statement.executeUpdate()

                argon2.wipeArray(passwordArray)

                ctx
                    .status(HttpStatus.OK_200)
                    .json(Response(null))
            }
        }
    }
    fun changePassword(ctx: Context) {
        val user = userFromWebToken(ctx.cookie(COOKIE_NAME)!!)!!

        val (oldPassword, newPassword) = ctx.bodyAsClass<ChangePasswordRequest>()

        if (oldPassword == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("Old password must be provided."))
        } else if (newPassword == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("New password must be provided."))
        } else if (newPassword.length < 8) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("New password must be at least 8 characters long."))
        } else if (newPassword.length > 2048) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(Response("New password cannot be longer than 2048 characters."))
        } else {
            oldPassword.toCharArray().let {
                val passwordIsCorrect = oldPassword.length <= 2048 && argon2.verify(user.password, it)

                argon2.wipeArray(it)

                if (!passwordIsCorrect) {
                    ctx
                        .status(HttpStatus.UNAUTHORIZED_401)
                        .json(Response("Incorrect old password."))

                    return
                }
            }
            newPassword.toCharArray().let {
                val statement = database!!.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")
                statement.setString(1, argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, it))
                statement.setString(2, user.userId)

                argon2.wipeArray(it)

                statement.executeUpdate()
            }

            var statement = database!!.prepareStatement("DELETE FROM web_tokens WHERE user_id = ?")
            statement.setString(1, user.userId)

            statement.executeUpdate()

            val webToken = UUID.randomUUID().toString()
            val cookie = Cookie(
                COOKIE_NAME,
                webToken,
                COOKIE_PATH,
                COOKIE_MAX_AGE,
                COOKIE_SECURE,
                COOKIE_VERSION,
                COOKIE_HTTP_ONLY,
                null,
                null,
                COOKIE_SAME_SITE
            )

            statement = database.prepareStatement("INSERT INTO web_tokens VALUES (?, ?, ?)")
            statement.setString(1, webToken)
            statement.setString(2, user.userId)
            statement.setLong(3, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8))

            statement.executeUpdate()

            ctx
                .status(HttpStatus.OK_200)
                .cookie(cookie)
                .json(Response(null))
        }
    }
    fun logout(ctx: Context) {
        val statement = database!!.prepareStatement("DELETE FROM web_tokens WHERE web_token = ?")
        statement.setString(1, ctx.cookie(COOKIE_NAME))

        statement.executeUpdate()

        val cookie = Cookie(
            COOKIE_NAME,
            "",
            COOKIE_PATH,
            0,
            COOKIE_SECURE,
            COOKIE_VERSION,
            COOKIE_HTTP_ONLY,
            null,
            null,
            COOKIE_SAME_SITE
        )

        ctx
            .status(HttpStatus.OK_200)
            .cookie(cookie)
            .json(Response(null))
    }
    fun revokeAllSessions(ctx: Context) {
        var webToken = ctx.cookie(COOKIE_NAME)!!
        val user = userFromWebToken(webToken)!!

        var statement = database!!.prepareStatement("DELETE FROM web_tokens WHERE user_id = ?")
        statement.setString(1, user.userId)

        statement.executeUpdate()

        webToken = UUID.randomUUID().toString()
        val cookie = Cookie(
            COOKIE_NAME,
            webToken,
            COOKIE_PATH,
            COOKIE_MAX_AGE,
            COOKIE_SECURE,
            COOKIE_VERSION,
            COOKIE_HTTP_ONLY,
            null,
            null,
            COOKIE_SAME_SITE
        )

        statement = database.prepareStatement("INSERT INTO web_tokens VALUES (?, ?, ?)")
        statement.setString(1, webToken)
        statement.setString(2, user.userId)
        statement.setLong(3, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8))

        statement.executeUpdate()

        ctx
            .status(HttpStatus.OK_200)
            .cookie(cookie)
            .json(Response(null))
    }
    fun deleteAccount(ctx: Context) {
        val webToken = ctx.cookie(COOKIE_NAME)!!
        val user = userFromWebToken(webToken)!!

        var statement = database!!.prepareStatement("DELETE FROM web_tokens WHERE user_id = ?")
        statement.setString(1, user.userId)

        statement.executeUpdate()

        statement = database.prepareStatement("DELETE FROM users WHERE user_id = ?")
        statement.setString(1, user.userId)

        statement.executeUpdate()

        ctx
            .status(HttpStatus.OK_200)
            .json(Response(null))
    }
}
