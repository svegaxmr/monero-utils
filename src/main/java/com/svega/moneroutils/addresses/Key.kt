package com.svega.moneroutils.addresses

import com.svega.crypto.ed25519.ge_p3_tobytes
import com.svega.crypto.ed25519.ge_scalarmult_base
import com.svega.crypto.ed25519.objects.ge_p3
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.MoneroException
import java.io.Serializable

data class Key(val data: ByteArray): Serializable {
    var str: String = ""
        get() {
            if(field == "")
                field = BinHexUtils.binaryToHex(data)
            return field
        }
        private set
    constructor(str: String): this(BinHexUtils.hexToByteArray(str)){
        this.str = str
    }
    init {
        if(data.size != 32){
            throw MoneroException("Key data must be 32 bytes")
        }
    }
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

fun SecretKey.getPublic(): PublicKey{
    val point = ge_p3()
    ge_scalarmult_base(point, this.data)
    val public = ByteArray(32)
    ge_p3_tobytes(public, point)
    return PublicKey(public)
}