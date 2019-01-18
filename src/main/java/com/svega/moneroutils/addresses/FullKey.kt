package com.svega.moneroutils.addresses

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.slowhash.Keccak
import com.svega.moneroutils.exceptions.MoneroException
import java.io.Serializable

@ExperimentalUnsignedTypes
data class FullKey(val spend: KeyPair, val view: KeyPair) : Serializable {
    init {
        if (spend.public.data.size != 32)
            throw MoneroException("Spend public data is not 32 bytes!")
        if (view.public.data.size != 32)
            throw MoneroException("View public data is not 32 bytes!")
        if ((spend.secret != null) and (spend.secret?.data?.size != 32))
            throw MoneroException("Spend private data is not 32 bytes!")
        if ((view.secret != null) and (view.secret?.data?.size != 32))
            throw MoneroException("View private data is not 32 bytes!")
    }

    fun getAddressString(addressType: AddressType, netType: NetType): String {
        val toHash = "${netType.getPrefixStr(addressType)}${spend.public.str}${view.public.str}"
        val csum = Keccak.fullChecksum(BinHexUtils.hexToUByteArray(toHash)).asByteArray()
        return Base58.encode(toHash + BinHexUtils.binaryToHex(csum))
    }

    fun getAddressBytes(addressType: AddressType, netType: NetType): ByteArray {
        val toHash = "${netType.getPrefixStr(addressType)}${spend.public.str}${view.public.str}"
        val csum = Keccak.fullChecksum(BinHexUtils.hexToUByteArray(toHash))
        return BinHexUtils.hexToByteArray(toHash) + csum.asByteArray()
    }
}