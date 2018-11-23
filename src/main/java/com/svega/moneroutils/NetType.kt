package com.svega.moneroutils

import com.svega.common.math.*

enum class NetType(mainaddr: Int, integrated: Int, subaddr: Int, val netID: ByteArray, val p2pPort: Int, val rpcPort: Int, val zmqPort: Int,
                   val genesisTX: String, val genesisNonce: Long) {
    MAINNET(18, 19, 42,
            byteArrayOf(0x12 ,0x30, 0xF1.toByte(), 0x71 , 0x61, 0x04 , 0x41, 0x61, 0x17, 0x31, 0x00, 0x82.toByte(), 0x16, 0xA1.toByte(), 0xA1.toByte(), 0x10),
            18080, 18081, 18082,
            "013c01ff0001ffffffffffff03029b2e4c0281c0b02e7c53291a94d1d0cbff8883f8024f5142ee494ffbbd08807121017767aafcde9be00dcfd098715ebcf7f410daebc582fda69d24a28e9d0bc890d1",
            10000),
    TESTNET(53, 54, 63,
            byteArrayOf(0x12 ,0x30, 0xF1.toByte(), 0x71 , 0x61, 0x04 , 0x41, 0x61, 0x17, 0x31, 0x00, 0x82.toByte(), 0x16, 0xA1.toByte(), 0xA1.toByte(), 0x11),
            28080, 28081, 28082,
            "013c01ff0001ffffffffffff03029b2e4c0281c0b02e7c53291a94d1d0cbff8883f8024f5142ee494ffbbd08807121017767aafcde9be00dcfd098715ebcf7f410daebc582fda69d24a28e9d0bc890d1",
            10001),
    STAGENET(24, 25, 36,
            byteArrayOf(0x12 ,0x30, 0xF1.toByte(), 0x71 , 0x61, 0x04 , 0x41, 0x61, 0x17, 0x31, 0x00, 0x82.toByte(), 0x16, 0xA1.toByte(), 0xA1.toByte(), 0x12),
            38080, 38081, 38082,
            "013c01ff0001ffffffffffff0302df5d56da0c7d643ddd1ce61901c7bdc5fb1738bfe39fbe69c28a3a7032729c0f2101168d0c4ca86fb55a4cf6a36d31431be1c53a3bd7411bb24e8832410289fa6f3b",
            10002);

    val MAINADDR = mainaddr.toUInt8()
    val INTEGRATED = integrated.toUInt8()
    val SUBADDR = subaddr.toUInt8()

    fun getPrefix(type: AddressType): UInt8 {
        return when(type){
            AddressType.MAIN -> this.MAINADDR
            AddressType.INTEGRATED -> this.INTEGRATED
            AddressType.SUBADDRESS -> this.SUBADDR
        }
    }

    fun getPrefixStr(type: AddressType) = BinHexUtils.binaryToHex(Array(1) {getPrefix(type)})
}