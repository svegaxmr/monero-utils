package com.svega.moneroutils.addresses

import com.svega.crypto.ed25519.ge_p3_tobytes
import com.svega.crypto.ed25519.ge_scalarmult_base
import com.svega.crypto.ed25519.objects.ge_p3
import java.io.Serializable

data class KeyPair(val public: PublicKey, val secret: SecretKey?): Serializable{
    companion object {
        fun genFromSecret(secret: ByteArray): KeyPair {
            val point = ge_p3()
            ge_scalarmult_base(point, secret)
            val public = ByteArray(32)
            ge_p3_tobytes(public, point)
            return KeyPair(PublicKey(public), SecretKey(secret))
        }
    }
}