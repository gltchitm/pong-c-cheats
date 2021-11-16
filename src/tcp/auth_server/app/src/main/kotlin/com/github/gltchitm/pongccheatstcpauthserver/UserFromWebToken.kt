package com.github.gltchitm.pongccheatstcpauthserver

fun userFromWebToken(webToken: String): User? {
    var statement = database!!.prepareStatement("SELECT * FROM web_tokens WHERE web_token = ?")
    statement.setString(1, webToken)

    val webTokenData = statement.executeQuery()

    if (webTokenData.next() && webTokenData.getLong(3) >= System.currentTimeMillis()) {
        statement = database.prepareStatement("SELECT * FROM users WHERE user_id = ?")
        statement.setString(1, webTokenData.getString(2))

        val user = statement.executeQuery()

        if (user.next()) {
            return User(user.getString(1), user.getString(2), user.getString(3))
        }
    }

    return null
}
