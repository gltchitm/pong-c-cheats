package com.github.gltchitm.pongccheatstcpauthserver

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.TimeUnit
import java.util.UUID

object TokenManager {
    private val tokens = HashMap<String, Long>()
    private val tokensLock = ReentrantReadWriteLock()

    fun newToken(): String? {
        try {
            tokensLock.writeLock().lock()

            purgeExpiredTokens()

            if (tokens.size > 200) {
                return null
            }

            val token = UUID.randomUUID().toString()

            tokens.put(token, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30))

            return token
        } finally {
            tokensLock.writeLock().unlock()
        }
    }
    fun redeemToken(token: String): Boolean {
        try {
            tokensLock.writeLock().lock()

            val expires = tokens.get(token)

            if (expires != null) {
                tokens.remove(token)

                return expires >= System.currentTimeMillis()
            }

            return false
        } finally {
            tokensLock.writeLock().unlock()
        }
    }
    fun purgeExpiredTokens() {
        try {
            tokensLock.writeLock().lock()

            tokens.entries.removeIf { (_, expires) -> expires <= System.currentTimeMillis() }
        } finally {
            tokensLock.writeLock().unlock()
        }
    }
}
