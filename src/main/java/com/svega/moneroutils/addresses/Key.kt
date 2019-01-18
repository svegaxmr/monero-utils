package com.svega.moneroutils.addresses

import com.svega.crypto.ed25519.ge
import com.svega.crypto.ed25519.ge_p3
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.exceptions.MoneroException
import java.io.Serializable

@ExperimentalUnsignedTypes
data class Key(val data: ByteArray) : Serializable {
    var str: String = ""
        get() {
            if (field.isEmpty())
                field = BinHexUtils.binaryToHex(data)
            return field
        }
        private set

    constructor(str: String) : this(BinHexUtils.hexToByteArray(str)) {
        this.str = str
    }

    init {
        if (data.size != 32) {
            throw MoneroException("Key data must be 32 bytes")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        if (other !is Key)
            return false
        if (!other.data.contentEquals(this.data))
            return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

@ExperimentalUnsignedTypes
typealias PublicKey = Key

@ExperimentalUnsignedTypes
typealias SecretKey = Key

@ExperimentalUnsignedTypes
fun SecretKey.getPublic(): PublicKey {
    val point = ge_p3()
    ge.scalarmult_base(point, this.data.asUByteArray())
    val public = UByteArray(32)
    ge.p3_tobytes(public, point)
    return PublicKey(public.asByteArray())
}