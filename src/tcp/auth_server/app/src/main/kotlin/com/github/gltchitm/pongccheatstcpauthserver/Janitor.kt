package com.github.gltchitm.pongccheatstcpauthserver

import kotlin.concurrent.thread

fun startJanitor() {
    thread {
        while (true) {
            val statement = database!!.prepareStatement("DELETE FROM web_tokens WHERE expires <= ?")
            statement.setLong(1, System.currentTimeMillis())
            statement.executeUpdate()

            TokenManager.purgeExpiredTokens()

            Thread.sleep(3600000)
        }
    }
}
