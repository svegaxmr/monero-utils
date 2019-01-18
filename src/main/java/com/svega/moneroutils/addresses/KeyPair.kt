package com.svega.moneroutils.addresses

import java.io.Serializable

@ExperimentalUnsignedTypes
data class KeyPair(val public: PublicKey, val secret: SecretKey?) : Serializable {
    companion object {
        fun genFromSecret(secret: ByteArray): KeyPair {
            val skey = SecretKey(secret)
            return KeyPair(skey.getPublic(), skey)
        }
    }
}