/*
 * Copyright (c) 2018-2019, Sergio Vega
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.svega.moneroutils

@ExperimentalUnsignedTypes
enum class NetType(mainaddr: Int, integrated: Int, subaddr: Int, val netID: UByteArray, val p2pPort: Int, val rpcPort: Int, val zmqPort: Int,
                   val genesisTX: String, val genesisNonce: Long) {
    MAINNET(18, 19, 42,
            ubyteArrayOf(0x12u, 0x30u, 0xF1u, 0x71u, 0x61u, 0x04u, 0x41u, 0x61u, 0x17u, 0x31u, 0x00u, 0x82u, 0x16u, 0xA1u, 0xA1u, 0x10u),
            18080, 18081, 18082,
            "013c01ff0001ffffffffffff03029b2e4c0281c0b02e7c53291a94d1d0cbff8883f8024f5142ee494ffbbd08807121017767aafcde9be00dcfd098715ebcf7f410daebc582fda69d24a28e9d0bc890d1",
            10000),
    TESTNET(53, 54, 63,
            ubyteArrayOf(0x12u, 0x30u, 0xF1u, 0x71u, 0x61u, 0x04u, 0x41u, 0x61u, 0x17u, 0x31u, 0x00u, 0x82u, 0x16u, 0xA1u, 0xA1u, 0x11u),
            28080, 28081, 28082,
            "013c01ff0001ffffffffffff03029b2e4c0281c0b02e7c53291a94d1d0cbff8883f8024f5142ee494ffbbd08807121017767aafcde9be00dcfd098715ebcf7f410daebc582fda69d24a28e9d0bc890d1",
            10001),
    STAGENET(24, 25, 36,
            ubyteArrayOf(0x12u, 0x30u, 0xF1u, 0x71u, 0x61u, 0x04u, 0x41u, 0x61u, 0x17u, 0x31u, 0x00u, 0x82u, 0x16u, 0xA1u, 0xA1u, 0x12u),
            38080, 38081, 38082,
            "013c01ff0001ffffffffffff0302df5d56da0c7d643ddd1ce61901c7bdc5fb1738bfe39fbe69c28a3a7032729c0f2101168d0c4ca86fb55a4cf6a36d31431be1c53a3bd7411bb24e8832410289fa6f3b",
            10002);

    val MAINADDR = mainaddr.toUByte()
    val INTEGRATED = integrated.toUByte()
    val SUBADDR = subaddr.toUByte()

    fun getPrefix(type: AddressType): UByte {
        return when (type) {
            AddressType.MAIN -> this.MAINADDR
            AddressType.INTEGRATED -> this.INTEGRATED
            AddressType.SUBADDRESS -> this.SUBADDR
        }
    }

    fun getPrefixStr(type: AddressType) = BinHexUtils.binaryToHex(ByteArray(1) { getPrefix(type).toByte() })
}