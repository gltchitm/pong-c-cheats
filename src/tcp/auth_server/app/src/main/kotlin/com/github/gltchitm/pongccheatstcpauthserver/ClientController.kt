package com.github.gltchitm.pongccheatstcpauthserver

import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus
import java.util.UUID

object ClientController {
    private data class LoginResponse(val token: String?, val message: String?)

    private data class LoginRequest(val username: String?, val password: String?)

    fun login(ctx: Context) {
        val (username, password) = ctx.bodyAsClass<LoginRequest>()

        if (username == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(LoginResponse(null, "Username must be provided."))

            return
        } else if (password == null) {
            ctx
                .status(HttpStatus.BAD_REQUEST_400)
                .json(LoginResponse(null, "Password must be provided."))

            return
        } else if (username.length <= 32 && password.length <= 2048) {
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


                    val token = TokenManager.newToken()

                    if (token == null) {
                        ctx
                            .status(HttpStatus.INSUFFICIENT_STORAGE_507)
                            .json(LoginResponse(null, "Too many tokens are unredeemed."))
                    } else {
                        ctx
                            .status(HttpStatus.OK_200)
                            .json(LoginResponse(token, null))
                    }

                    return
                }
            }
        }

        ctx
            .status(HttpStatus.UNAUTHORIZED_401)
            .json(LoginResponse(null, "Incorrect username or password."))
    }
}
