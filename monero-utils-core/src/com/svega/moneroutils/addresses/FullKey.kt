package com.svega.moneroutils.addresses

import com.svega.common.math.asByteArray
import com.svega.crypto.common.algos.Keccak
import com.svega.moneroutils.*
import java.io.Serializable

data class FullKey(val spend: KeyPair, val view: KeyPair): Serializable{
    init {
        if(spend.public.data.size != 32)
            throw MoneroException("Spend public data is not 32 bytes!")
        if(view.public.data.size != 32)
            throw MoneroException("View public data is not 32 bytes!")
        if(spend.secret?.data?.size != 32)
            throw MoneroException("Spend private data is not 32 bytes!")
        if(view.secret?.data?.size != 32)
            throw MoneroException("View private data is not 32 bytes!")
    }
    fun getAddressString(addressType: AddressType, netType: NetType): String{
        val toHash = "${netType.getPrefixStr(addressType)}${spend.public.str}${view.public.str}"
        val csum = Keccak.checksum(BinHexUtils.hexToBinary(toHash))
        return Base58.encode(toHash + BinHexUtils.binaryToHex(csum))
    }
    fun getAddressBytes(addressType: AddressType, netType: NetType): ByteArray{
        val toHash = "${netType.getPrefixStr(addressType)}${spend.public.str}${view.public.str}"
        val csum = Keccak.checksum(BinHexUtils.hexToBinary(toHash))
        return BinHexUtils.hexToByteArray(toHash) + csum.asByteArray()
    }
}