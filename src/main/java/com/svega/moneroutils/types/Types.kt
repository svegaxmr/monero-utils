package com.svega.moneroutils.types

import com.svega.moneroutils.exceptions.MoneroException

typealias CumulativeDifficulty = ULong

data class CryptoHash(val data: ByteArray) {
    constructor(): this(ByteArray(32))
    init {
        if(data.size != 32) {
            throw MoneroException("Crypto Hash must be 32 bytes")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CryptoHash

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

data class KeyImage(val data: ByteArray) {
    constructor(): this(ByteArray(32))
    init {
        if(data.size != 32) {
            throw MoneroException("Crypto Hash must be 32 bytes")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyImage

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}