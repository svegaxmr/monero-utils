package com.svega.moneroutils.transactions

import com.svega.moneroutils.BinHexUtils
import java.util.*

data class PaymentID(val encrypted: Boolean, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentID

        if (encrypted != other.encrypted) return false
        if (!Arrays.equals(data, other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encrypted.hashCode()
        result = 31 * result + Arrays.hashCode(data)
        return result
    }

    override fun toString(): String {
        return "PaymentID(encrypted=$encrypted, data=${BinHexUtils.binaryToHex(data)})"
    }
}