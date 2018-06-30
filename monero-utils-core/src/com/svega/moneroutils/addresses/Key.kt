package com.svega.moneroutils.addresses

import com.svega.moneroutils.BinHexUtils
import java.io.Serializable

data class Key(val data: ByteArray, val str: String): Serializable {
    constructor(data: ByteArray): this(data, BinHexUtils.binaryToHex(data))
    constructor(str: String): this(BinHexUtils.hexToByteArray(str), str)
    override fun equals(other: Any?): Boolean{
        if(other == null)
            return false
        if(other !is Key)
            return false
        if(!other.data.contentEquals(this.data))
            return false
        return true
    }
    override fun hashCode(): Int {
        return super.hashCode()
    }
}

typealias PublicKey = Key
typealias SecretKey = Key