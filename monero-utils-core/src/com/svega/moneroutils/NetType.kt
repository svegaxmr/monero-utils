package com.svega.moneroutils

import com.svega.common.math.*

enum class NetType(mainaddr: Int, integrated: Int, subaddr: Int) {
    MAINNET(18, 19, 42),
    TESTNET(53, 54, 63),
    STAGENET(24,25,36);

    val MAINADDR = mainaddr.toUInt8()
    val INTEGRATED = integrated.toUInt8()
    val SUBADDR = subaddr.toUInt8()

    fun getPrefix(type: AddressType): UInt8 {
        return when(type){
            AddressType.MAIN -> this.MAINADDR
            AddressType.INTEGATED -> this.INTEGRATED
            AddressType.SUBADDRESS -> this.SUBADDR
        }
    }

    fun getPrefixStr(type: AddressType) = BinHexUtils.binaryToHex(Array(1, {getPrefix(type)}))
}