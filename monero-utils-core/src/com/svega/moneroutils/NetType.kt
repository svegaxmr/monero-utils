package com.svega.moneroutils

enum class NetType {
    MAINNET(18, 19, 42),
    TESTNET(53, 54, 63),
    STAGENET(24,25,36);
    constructor(mainnet: Int, integrated: Int, subaddr: Int){
        this.MAINADDR = mainnet.toUInt8()
        this.INTEGRATED = integrated.toUInt8()
        this.SUBADDR = subaddr.toUInt8()
    }
    val MAINADDR: UInt8
    val INTEGRATED: UInt8
    val SUBADDR: UInt8
}